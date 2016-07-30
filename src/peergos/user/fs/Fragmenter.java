package peergos.user.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import peergos.util.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public interface Fragmenter {

    byte[][] split(byte[] input, boolean isJavascript);

    byte[] recombine(byte[][] encoded, int inputLength, boolean useJavascript);

    void serialize(DataSink dout);

    static Fragmenter deserialize(DataInput din) throws IOException {
        int val = din.readInt();
        Type type  = Type.ofVal(val);
        switch (type) {
            case SIMPLE:
                return new peergos.user.fs.SplitFragmenter();
            case ERASURE_CODING:
                int nOriginalFragments = din.readInt();
                int nAllowedFailures = din.readInt();
                return new peergos.user.fs.ErasureFragmenter(nOriginalFragments, nAllowedFailures);
            default:
                throw new IllegalStateException();
        }
    }

    enum Type  {
        SIMPLE(0),
        ERASURE_CODING(1);

        public final int val;

        Type(int val) {
            this.val = val;
        }

        private static Map<Integer, Type> MAP = Stream.of(values())
                .collect(
                        Collectors.toMap(
                                e -> e.val,
                                e -> e));
        public static Type ofVal(int val) {
            Type type = MAP.get(val);
            if (type == null)
                throw new IllegalStateException("No type for value "+ val);
            return type;
        }
    }

    @RunWith(Parameterized.class)
    class FragmenterTest  {
        private static Random random = new Random(666);

        private final Fragmenter  fragmenter;

        public  FragmenterTest(Fragmenter fragmenter) {
            this.fragmenter = fragmenter;
        }

        @Parameterized.Parameters(name = "{0}")
        public static Collection<Object[]> parameters() {
            return Arrays.asList(new Object[][]{
                    {new SplitFragmenter()},
                    {new peergos.user.fs.ErasureFragmenter(EncryptedChunk.ERASURE_ORIGINAL, EncryptedChunk.ERASURE_ALLOWED_FAILURES)}
            });
        }

        @Test
        public void testSeries() throws IOException {
            for (int i = 1; i < 10; i++) {
                int length = random.nextInt(Chunk.MAX_SIZE);
                byte[] b = new byte[length];
                test(b);
            }
        }
        @Test public void testBoundary()  throws IOException {
            List<Integer> sizes = Arrays.asList(Fragment.MAX_LENGTH, 2 * Fragment.MAX_LENGTH);
            for (Integer size : sizes) {
                byte[] b = new byte[size];
                test(b);
            }
        }

        private void test(byte[] input)  throws IOException {
            random.nextBytes(input);


            byte[][] split = fragmenter.split(input, false);

//            int nChunk  = input.length / Chunk.MAX_SIZE;
//            if (input.length % Chunk.MAX_SIZE > 0)
//                nChunk++;
//
//            assertEquals(split.length, nChunk);

            for (byte[] bytes : split) {
                int length = bytes.length;
                assertTrue(length > 0);
                assertTrue(length <= Fragment.MAX_LENGTH);
            }

            byte[] recombine = fragmenter.recombine(split, input.length, false);

            assertTrue("recombine(split(input)) = input", Arrays.equals(input, recombine));
        }


        @Test public void serializationTest()  throws IOException {
            DataSink sink = new DataSink();
            fragmenter.serialize(sink);

            Fragmenter deserialize = Fragmenter.deserialize(new DataInputStream(
                    new ByteArrayInputStream(sink.toByteArray())));

            assertEquals(fragmenter, deserialize);
        }
    }
}
