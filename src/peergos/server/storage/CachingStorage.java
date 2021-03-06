package peergos.server.storage;

import peergos.shared.ipfs.api.Multihash;
import peergos.shared.merklebtree.MerkleNode;
import peergos.shared.storage.ContentAddressedStorage;
import peergos.shared.util.*;

public class CachingStorage implements ContentAddressedStorage {
    private final ContentAddressedStorage target;
    private final LRUCache<Multihash, byte[]> cache;
    private final int maxValueSize;

    public CachingStorage(ContentAddressedStorage target, int cacheSize, int maxValueSize) {
        this.target = target;
        this.cache = new LRUCache(cacheSize);
        this.maxValueSize = maxValueSize;
    }

    @Override
    public Multihash put(MerkleNode object) {
        return target.put(object);
    }

    @Override
    public byte[] get(Multihash key) {
        if (cache.containsKey(key))
            return cache.get(key);
        byte[] value = target.get(key);
        if (value.length > 0 && value.length < maxValueSize)
            cache.put(key, value);
        return value;
    }

    @Override
    public boolean recursivePin(Multihash h) {
        return target.recursivePin(h);
    }

    @Override
    public boolean recursiveUnpin(Multihash h) {
        return target.recursiveUnpin(h);
    }
}
