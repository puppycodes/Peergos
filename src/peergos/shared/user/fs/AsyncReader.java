package peergos.shared.user.fs;

import jsinterop.annotations.*;

import java.util.concurrent.*;

@JsType
public interface AsyncReader extends AutoCloseable {

    CompletableFuture<Boolean> seek(int high32, int low32);

    /**
     *
     * @param res array to store data in
     * @param offset initial index to store data in res
     * @param length number of bytes to read
     * @return number of bytes read
     */
    CompletableFuture<Integer> readIntoArray(byte[] res, int offset, int length);

    /**
     *  reset to original starting position
     * @return
     */
    CompletableFuture<Boolean> reset();

    /**
     * Close and dispose of any resources
     */
    void close();

    class ArrayBacked implements AsyncReader {
        private final byte[] data;
        private int index = 0;

        public ArrayBacked(byte[] data) {
            this.data = data;
        }

        @Override
        public CompletableFuture<Boolean> seek(int high32, int low32) {
            if (high32 != 0)
                throw new IllegalArgumentException("Cannot have arrays larger than 4GiB!");
            index += low32;
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Integer> readIntoArray(byte[] res, int offset, int length) {
            System.arraycopy(data, index, res, offset, length);
            index += length;
            return CompletableFuture.completedFuture(length);
        }

        @Override
        public CompletableFuture<Boolean> reset() {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public void close() {
        }
    }
}
