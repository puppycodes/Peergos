package peergos.shared.user;

import peergos.shared.crypto.*;
import peergos.shared.ipfs.api.*;
import peergos.shared.user.fs.*;

import java.util.*;
import java.util.concurrent.*;

public class WriterData {
    /**
     *  Represents the merkle node that a public key maps to
     */

    // publicly readable and present on owner keys
    public final Optional<UserGenerationAlgorithm> generationAlgorithm;
    // accessible under IPFS address $hash/public
    public final Optional<ReadableFilePointer> publicData;
    // accessible under IPFS address $hash/owned
    public final Set<UserPublicKey> ownedKeys;

    // Encrypted
    // accessible under IPFS address $hash/static (present on owner keys)
    public final Optional<UserStaticData> staticData;
    // accessible under IPFS address $hash/btree (present on writer keys)
    public final Optional<Multihash> btree;

    /**
     *
     * @param generationAlgorithm The algorithm used to create the users key pairs and root key from the username and password
     * @param publicData A readable pointer to a subtree made public by this key
     * @param ownedKeys Any public keys owned by this key
     * @param staticData Any static data owner by this key (list of entry points)
     * @param btree Any file tree owned by this key
     */
    public WriterData(Optional<UserGenerationAlgorithm> generationAlgorithm, Optional<ReadableFilePointer> publicData,
                      Set<UserPublicKey> ownedKeys, Optional<UserStaticData> staticData, Optional<Multihash> btree) {
        this.generationAlgorithm = generationAlgorithm;
        this.publicData = publicData;
        this.ownedKeys = ownedKeys;
        this.staticData = staticData;
        this.btree = btree;
    }
}
