package peergos.shared.user.fs;

import java.util.concurrent.CompletableFuture;

import jsinterop.annotations.JsType;

@JsType(namespace = "thumbnail", isNative = true)
public class NativeJSThumbnail {
//public byte[] generateThumbnail(InputStream imageBlob, String fileName)
	public native CompletableFuture<byte[]> generateThumbnail(byte[] imageBlob, String fileName) ;
}
