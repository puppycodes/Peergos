package peergos.server.net;

import peergos.corenode.CoreNode;
import peergos.corenode.HTTPCoreNode;
import peergos.server.storage.IpfsDHT;
import peergos.server.storage.ContentAddressedStorage;
import peergos.server.merklebtree.MerkleBTree;
import peergos.util.ArrayOps;
import peergos.util.ByteArrayWrapper;

import java.io.IOException;

public class BTreeTest {
    public static void main(String[] args) throws IOException {
        byte[] writer = ArrayOps.hexToBytes("0d20ea71c9f723c1cdb42bc134c9f0f145a583ca96529a90c91456f4ec255f061e3d90c81293c7b31260e6b4355ff522e65717bc69e722341717dee74c5b234d");
        byte[] mapKey = ArrayOps.hexToBytes("adb6c0fbdf4a6512fd28a34f9b71687dea92416c86fc65d77af43309ba6bbb92");

        CoreNode core = HTTPCoreNode.getInstance();
        ContentAddressedStorage dht = new IpfsDHT();
        byte[] rootHash = core.getMetadataBlob(writer);
        MerkleBTree btree = MerkleBTree.create(rootHash, dht);
        btree.print(System.out);
        byte[] value = btree.get(mapKey);
        System.out.println(value == null ? value : new ByteArrayWrapper(value));
    }
}