package peergos.user.fs;

import org.ipfs.api.*;
import peergos.crypto.*;
import peergos.crypto.symmetric.*;
import peergos.user.*;
import peergos.user.fs.erasure.*;
import peergos.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class EncryptedChunkRetriever implements FileRetriever {

    private final byte[] chunkNonce, chunkAuth;
    private final int nOriginalFragments, nAllowedFailures;
    private final List<Multihash> fragmentHashes;
    private final Location nextChunk;

    public EncryptedChunkRetriever(byte[] chunkNonce, byte[] chunkAuth, List<Multihash> fragmentHashes, Location nextChunk, int nOriginalFragments, int nAllowedFailures) {
        this.chunkNonce = chunkNonce;
        this.chunkAuth = chunkAuth;
        this.nOriginalFragments = nOriginalFragments;
        this.nAllowedFailures = nAllowedFailures;
        this.fragmentHashes = fragmentHashes;
        this.nextChunk = nextChunk;
    }

    public LazyInputStreamCombiner getFile(UserContext context, SymmetricKey dataKey, long len, Consumer<Long> monitor) {
        byte[] chunk = getChunkInputStream(context, dataKey, len, monitor);
        return new LazyInputStreamCombiner(this, context, dataKey, chunk, monitor);
    }

    public Location getNext() {
        return this.nextChunk;
    }

    public byte[] getChunkInputStream(UserContext context, SymmetricKey dataKey, int len, Consumer<Long> monitor) {
        List<Fragment> fragments = context.downloadFragments(fragmentHashes, monitor);
        fragments = reorder(fragments, fragmentHashes);
        byte[] cipherText = Erasure.recombine(fragments.stream().map(f -> f.data.data).collect(Collectors.toList()), len != 0 ? len : Chunk.MAX_SIZE, nOriginalFragments, nAllowedFailures);
        if (len != 0)
            cipherText = Arrays.copyOfRange(cipherText, 0, len);
        EncryptedChunk fullEncryptedChunk = new EncryptedChunk(ArrayOps.concat(chunkAuth, cipherText));
        byte[] original = fullEncryptedChunk.decrypt(dataKey, chunkNonce);
        return original;
    }

    public void serialize(DataSink buf) throws IOException {
        buf.writeByte((byte)1); // This class
        buf.writeArray(chunkNonce);
        buf.writeArray(chunkAuth);
        buf.writeArray(ArrayOps.concat(fragmentHashes.stream().map(h -> new ByteArrayWrapper(h.toBytes())).collect(Collectors.toList())));
        buf.writeByte(this.nextChunk != null ? (byte)1 : 0);
        if (this.nextChunk != null)
            buf.write(this.nextChunk.serialize());
        buf.writeInt(this.nOriginalFragments);
        buf.writeInt(this.nAllowedFailures);
    }

    public static EncryptedChunkRetriever deserialize(DataSource buf) throws IOException {
        byte[] chunkNonce = buf.readArray();
        byte[] chunkAuth = buf.readArray();
        byte[] concatFragmentHashes = buf.readArray();
        List<byte[]> fragmentHashes = split(concatFragmentHashes, Hash.HASH_BYTES);
        boolean hasNext = buf.readBoolean();
        Location nextChunk = null;
        if (hasNext)
            nextChunk = Location.deserialize(buf);
        int nOriginalFragments = buf.readInt();
        int nAllowedFailures = buf.readInt();
        if (!EncryptedChunk.ALLOWED_ORIGINAL.contains(nOriginalFragments) || !EncryptedChunk.ALLOWED_FAILURES.contains(nAllowedFailures)) {
            // backwards compatible with when these were not included
            buf.skip(-8);
            nOriginalFragments = EncryptedChunk.ERASURE_ORIGINAL;
            nAllowedFailures = EncryptedChunk.ERASURE_ALLOWED_FAILURES;
        }
        List<Multihash> hashes = fragmentHashes.stream().map(b -> new Multihash(b)).collect(Collectors.toList());
        return new EncryptedChunkRetriever(chunkNonce, chunkAuth, hashes, nextChunk, nOriginalFragments, nAllowedFailures);
    }

    private static List<byte[]> split(byte[] arr, int size) {
        int length = arr.length/size;
        List<byte[]> res = new ArrayList<>();
        for (int i=0; i < length; i++)
            res.add(Arrays.copyOfRange(arr, i*size, (i+1)*size));
        return res;
    }
}