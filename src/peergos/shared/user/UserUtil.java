package peergos.shared.user;

import peergos.shared.crypto.User;
import peergos.shared.crypto.asymmetric.curve25519.*;
import peergos.shared.crypto.hash.*;
import peergos.shared.crypto.random.*;
import peergos.shared.crypto.symmetric.*;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class UserUtil {

    public static CompletableFuture<UserWithRoot> generateUser(String username, String password, LoginHasher hasher,
                                            Salsa20Poly1305 provider, SafeRandom random, Ed25519 signer, Curve25519 boxer) {
    	CompletableFuture<byte[]> fut = hasher.hashToKeyBytes(username, password);
    	return fut.thenApply(keyBytes -> {
	        byte[] signBytesSeed = Arrays.copyOfRange(keyBytes, 0, 32);
	        byte[] secretBoxBytes = Arrays.copyOfRange(keyBytes, 32, 64);
	        byte[] rootKeyBytes = Arrays.copyOfRange(keyBytes, 64, 96);
	
	        byte[] secretSignBytes = Arrays.copyOf(signBytesSeed, 64);
	        byte[] publicSignBytes = new byte[32];
	
	        signer.crypto_sign_keypair(publicSignBytes, secretSignBytes);
	
	        byte[] pubilcBoxBytes = new byte[32];
	        boxer.crypto_box_keypair(pubilcBoxBytes, secretBoxBytes);
	
	        User user = new User(
	                new Ed25519SecretKey(secretSignBytes, signer),
	                new Curve25519SecretKey(secretBoxBytes, boxer),
	                new Ed25519PublicKey(publicSignBytes, signer),
	                new Curve25519PublicKey(pubilcBoxBytes, boxer, random));
	
	        SymmetricKey root =  new TweetNaClKey(rootKeyBytes, false, provider, random);

	        return new UserWithRoot(user, root);
    	});
    }
}
