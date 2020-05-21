package ttl.trywrap;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author whynot
 */
public class TestTryWrap {

    @Test
    public void testTryWrapHoldsValue() {
        String s = "hellod";
        TryWrap<String> me = TryWrap.ofRight(s);
        assertEquals(s, me.right());
    }

    @Test
    public void testGettingLeftOnASuccessfulTryWrapShouldThrowNoSuchElementException() {
        assertThrows(NoSuchElementException.class, () -> {
            TryWrap<String> me = TryWrap.of(() -> "abc");
            Exception e = me.left();
        });
    }


    @Test
    public void testTryWrapMapSuccess() {
        String s = "hello";
        TryWrap<String> me = TryWrap.ofRight(s);
        assertEquals(s, me.right());
        //convert it to a TryWrap<Integer, ...>
        TryWrap<Integer> m2 = me.map(str -> str.length());
        //map using a plain func
        TryWrap<Double> m3 = m2.map(this::plainFun);
        assertThat(m3.right(), greaterThan(0.0));
        //Don't do this!!!
        TryWrap<TryWrap<Integer>> m5 = m2.map(this::cube);

        //flatMap is called for when called fun returns a TryWrap
        TryWrap<Integer> m6 = m2.flatMap(this::cube);
        assertEquals(125, m6.right());
    }

    @Test
    public void testMapFailure() {
        TryWrap<Integer> me = TryWrap.ofRight(10);
        TryWrap<Integer> me2 = me.map(i -> i / 0);

        assertTrue(me2.isLeft());
    }

    @Test
    public void testMapOnAFailedTryReturnsFailure() {
        TryWrap<Integer> me = TryWrap.ofRight(10);
        TryWrap<Integer> me2 = me.map(i -> i / 0);

        assertTrue(me2.isLeft());

        TryWrap<Integer> me3 = me2.map(i -> i + 2);
        assertTrue(me2.isLeft());
    }

    @Test
    public void testMapOfEmptyReturnsEmpty() {
        TryWrap<String> me = TryWrap.of(() -> "abc").filter(s -> s.length() > 5);
        TryWrap<String> mapped = me.map(s -> s);

        assertTrue(mapped.isEmpty());
    }

    @Test
    public void testFlatMapFailure() {
        TryWrap<Integer> me = TryWrap.ofRight(10);
        TryWrap<Integer> me2 = me.flatMap(this::sqBad);
        assertTrue(me2.isLeft());

        TryWrap<Integer> me3 = me.flatMap(this::sq);
        assertTrue(me3.isRight());
    }

    @Test
    public void testTryWrapSupplierSuccess() {
        TryWrap<Integer> me = TryWrap.of(() -> {
            return 2;
        });

        assertTrue(me.isRight());
        assertEquals(2, me.right());
    }

    @Test
    public void testFlatMapOnAFailedTryReturnsFailure() {
        TryWrap<Integer> me = TryWrap.ofRight(10);
        TryWrap<Integer> me2 = me.map(i -> i / 0);

        assertTrue(me2.isLeft());

        TryWrap<Integer> me3 = me2.flatMap(i -> TryWrap.of(() -> i + 2));
        assertTrue(me2.isLeft());
    }

    @Test
    public void testFlatMapOfEmptyReturnsEmpty() {
        TryWrap<String> me = TryWrap.of(() -> "abc").filter(s -> s.length() > 5);
        TryWrap<String> mapped = me.flatMap(s -> TryWrap.of(() -> s));

        assertTrue(mapped.isEmpty());
    }

    @Test
    public void testTryWrapSupplierError() {
        TryWrap<Integer> me = TryWrap.of(() -> {
            return 2 / 0;
        });

        assertTrue(me.isLeft());
        assertTrue(me.left() instanceof ArithmeticException);
    }

    @Test
    public void testTryWrapRunnableShouldGiveEmptyTryWrap() throws Exception {
        TryWrap<?> tw = TryWrap.ofRunnable(() -> System.out.println("boo"));

        assertTrue(tw.isEmpty());

        tw.orElseConsume(e -> System.out.println(e));
    }

    @Test
    public void testTryWrapRunnableFailureShouldGiveFailureTryWrap() {
        assertThrows(ArithmeticException.class, () -> {
            TryWrap<?> tw = TryWrap.ofRunnable(() -> {
                int i = 10 / 0;
            });

            assertTrue(tw.isLeft());

            tw.orElseThrow();
        });
    }

    @Test
    public void testCallingRightOnEmptyShouldThrowException() {
        assertThrows(NoSuchElementException.class, () -> {
            TryWrap<?> tw = TryWrap.of(() -> 10 / 0);

            assertTrue(tw.isLeft());

            //This should throw any Exception
            tw.right();
        });
    }

    @Test
    public void testTryWrapShouldThrowEmptyExceptionOnOrElseThrowOnAnEmpty() {
        assertThrows(NoSuchElementException.class, () -> {
            TryWrap<?> tw = TryWrap.ofRunnable(() -> {
            });

            assertTrue(tw.isEmpty());

            tw.orElseThrow();
        });
    }

    @Test
    public void testFiltering() {
        List<String> fileNames = Arrays.asList("large", "small", "doesNotExist");
        List<TryWrap<byte[]>> tries = fileNames.stream()
                .map(fileName -> TryWrap.of(() -> getBytes(fileName)))
                .filter(mt -> mt.isRight())
                .collect(Collectors.toList());
        assertEquals(2, tries.size());
    }

    @Test
    public void testFilteringOnTryWrapWithFailingTestShouldGiveEmptyTryWrap() {
        TryWrap<byte[]> bytes = TryWrap.of(() -> getBytes("large"));
        TryWrap<byte[]> b2 = bytes.filter((b -> b.length < 10));

        assertTrue(b2.isEmpty());
    }

    @Test
    public void testFilteringOnTryWrapWithPassingTestShouldGiveFullTryWrap() {
        TryWrap<byte[]> bytes = TryWrap.of(() -> getBytes("large"));
        TryWrap<byte[]> b2 = bytes.filter((b -> b.length > 10));

        assertTrue(b2.isRight());
    }

    @Test
    public void testFilterOnAFailedTryReturnsFailure() {
        TryWrap<Integer> me = TryWrap.ofRight(10);
        TryWrap<Integer> me2 = me.map(i -> i / 0);

        assertTrue(me2.isLeft());

        TryWrap<Integer> me3 = me2.filter(i -> i < 2);
        assertTrue(me2.isLeft());
    }

    @Test
    public void testFilterOfEmptyReturnsEmpty() {
        TryWrap<String> me = TryWrap.of(() -> "abc").filter(s -> s.length() > 5);
        TryWrap<String> mapped = me.filter(s -> s.length() > 1);

        assertTrue(mapped.isEmpty());
    }

    @Test
    public void testExceptionOnFilterPredicateReturnsFailedTryWrap() {
        TryWrap<String> me = TryWrap.of(() -> "abc");
        TryWrap<String> mapped = me.filter(s -> s.length() > (1 / 0));

        assertTrue(mapped.isLeft());
    }

    @Test
    public void testIfPresentWithAFullTryWrapShouldRunTheConsumer() {
        TryWrap<String> me = TryWrap.of(() -> "abc");
        boolean[] consumerCalled = {false};
        me.ifPresent(s -> {
            consumerCalled[0] = true;
        });

        assertTrue(consumerCalled[0]);
    }

    @Test
    public void testIfPresentShouldNotCallConsumerWithFailedTryWrap() {
        TryWrap<Integer> me = TryWrap.of(() -> (10 / 0));
        boolean[] consumerCalled = {false};
        me.ifPresent(s -> {
            consumerCalled[0] = true;
        });

        assertFalse(consumerCalled[0]);
    }

    @Test
    public void testIfPresentShouldThrowUncheckedExceptionOnConsumerFailure() {
        assertThrows(RuntimeException.class, () -> {
            TryWrap<String> me = TryWrap.of(() -> "abc");
            me.ifPresent(s -> {
                int i = 10 / 0;
            });
        });
    }

    @Test
    public void testIfPresentOrElseConsumesWithFullTryWrapShouldRunSuccessConsumer() {
        TryWrap<String> me = TryWrap.of(() -> "abc");
        int[] consumerCalled = {0};
        me.ifPresentOrElseConsume(s -> {
            consumerCalled[0] = 2;
        }, s -> {
            consumerCalled[0] = 1;
        } );

        assertTrue(consumerCalled[0] == 2);
    }

    @Test
    public void testIfPresentOrElseConsumesWithFailedTryWrapShouldRunExConsumer() {
        TryWrap<Integer> me = TryWrap.of(() -> (10 / 0));
        int[] consumerCalled = {0};
        me.ifPresentOrElseConsume(s -> {
            consumerCalled[0] = 2;
        }, s -> {
            consumerCalled[0] = 1;
        } );

        assertTrue(consumerCalled[0] == 1);
    }

    /* Or Else Tests */

    @Test
    public void testOrElsePrintStackTrace() {
        TryWrap<Integer> me = TryWrap.of(() -> (10 / 0));
        me.orElsePrintStackTrace();

        //Don't know what to assert here, but we should
        //see coverage
    }

    @Test
    public void testOrElseThrowsShouldThrowAnIOExceptionOnFailedTryWrap() {
        assertThrows(IOException.class, () -> {
            String fileName = "doesNotExist";
            TryWrap<byte[]> me = TryWrap.of(() -> {
                byte[] bytes = getBytes(fileName);
                return bytes;
            });

            byte[] bytes = me.orElseThrow();
        });
    }

    @Test
    public void testOrElseThrowsShouldNotThrowAnIOExceptionOnSuccessTryWrap() throws Exception {
        String fileName = "large";
        TryWrap<byte[]> me = TryWrap.of(() -> {
            byte[] bytes = getBytes(fileName);
            return bytes;
        });

        byte[] bytes = me.orElseThrow();
        assertTrue(bytes.length == 45);
    }

    @Test
    public void testOrElseConsumeShouldThrowRuntimeExceptionOnConsumerFailure() {
        assertThrows(RuntimeException.class, () -> {
            String s = "hellod";
            TryWrap<Integer> me = TryWrap.of(() -> 10 / 0);

            me.orElseConsume(e -> {
                int i = 10 / 0;
            });
        });
    }


    @Test
    public void testOrElseNullShouldReturnValueOnSuccessTryWrap() {
        TryWrap<Integer> me = TryWrap.of(() -> 10);

        Integer it = me.orElseNull();
        assertEquals(10, it);
    }

    @Test
    public void testOrElseNullShouldReturnNullOnFailedTryWrap() {
        TryWrap<Integer> me = TryWrap.of(() -> 10 / 0);

        Integer it = me.orElseNull();
        assertEquals(null, it);
    }

    @Test
    public void testOrElseNullShouldReturnNullOnEmptyTryWrap() {
        TryWrap<Integer> me = TryWrap.ofEmpty();

        Integer it = me.orElseNull();
        assertEquals(null, it);
    }

    @Test
    public void testOrElseThrowUncheckedShouldReturnValueOnSuccessTryWrap() {
        TryWrap<Integer> me = TryWrap.ofRight(10);

        Integer it = me.orElseThrowUnchecked();
        assertEquals(10, it);
    }

    @Test
    public void testOrElseThrowUncheckedShouldThrowRuntimeExceptionOnFailedTryWrap() {
        assertThrows(RuntimeException.class, () -> {
            TryWrap<Integer> me = TryWrap.of(() -> 10 / 0);

            Integer it = me.orElseThrowUnchecked();
        });
    }

    @Test
    public void testOrElseThrowUncheckedShouldThrowNoSuchElementExceptionOnEmptyTryWrap() {
        assertThrows(NoSuchElementException.class, () -> {
            TryWrap<Integer> me = TryWrap.ofEmpty();

            Integer it = me.orElseThrowUnchecked();
        });
    }

    @Test
    public void testOrElseThrowUncheckedShouldConvertExistingCheckExceptionToRuntime() {
        assertThrows(RuntimeException.class, () -> {
            TryWrap<FileInputStream> me = TryWrap.of(() -> new FileInputStream("doesNotExist"));

            FileInputStream fis = me.orElseThrowUnchecked();
        });
    }

    @Test
    public void testOrElseThrowWithGivenExceptionReturnsValueOnSuccessTryWrap() throws Exception {
        String fileName = "pom.xml";
        TryWrap<FileInputStream> me = TryWrap.of(() -> new FileInputStream(fileName));

        FileInputStream fis = me.orElseThrow(new IOException(fileName + " Not Found"));

        assertTrue(fis != null);
    }
    @Test
    public void testOrElseThrowWithGivenExceptionThrowsTheExceptionOnFailureTryWrap() {
        String fileName = "doesNotExist";
        assertThrows(IOException.class, () -> {
            TryWrap<FileInputStream> me = TryWrap.of(() -> new FileInputStream(fileName));

            FileInputStream fis = me.orElseThrow(new IOException(fileName + " Not Found"));
        });
    }

    @Test
    public void testOrElseThrowWithGivenExceptionThrowsTheExceptionOnEmptyTryWrap() {
        assertThrows(RuntimeException.class, () -> {
            TryWrap<FileInputStream> me = TryWrap.ofEmpty();

            me.orElseThrow(new RuntimeException("Empty guy"));
        });
    }

    @Test
    public void testOrElseThrowUncheckedWithGivenExceptionReturnsValueOnSuccessTryWrap() throws Exception {
        String fileName = "pom.xml";
        TryWrap<FileInputStream> me = TryWrap.of(() -> new FileInputStream(fileName));

        FileInputStream fis = me.orElseThrowUnchecked(new IOException(fileName + " Not Found"));

        assertTrue(fis != null);
    }

    @Test
    public void testOrElseThrowUncheckedWithGivenExceptionThrowsRuntimeExceptionOnFailureTryWrap() {
        String fileName = "doesNotExist";
        assertThrows(RuntimeException.class, () -> {
            TryWrap<FileInputStream> me = TryWrap.of(() -> new FileInputStream(fileName));

            FileInputStream fis = me.orElseThrowUnchecked(new IOException(fileName + " Not Found"));
        });
    }

    @Test
    public void testOrElseThrowUncheckedWithGivenExceptionThrowsTheExceptionOnEmptyTryWrap() {
        assertThrows(RuntimeException.class, () -> {
            TryWrap<FileInputStream> me = TryWrap.ofEmpty();

            me.orElseThrowUnchecked(new RuntimeException("Empty guy"));
        });
    }

    public byte[] getBytes(String fileName) throws IOException, URISyntaxException {
        //URL url = FakeFileChannel.class.getClassLoader().getResource(fileName);
        URL url = getClass().getClassLoader().getResource(fileName);
        if (url != null) {
            Path path = Paths.get(url.toURI());
            byte[] bytes = Files.readAllBytes(path);
            return bytes;
        } else {
            throw new FileNotFoundException("File Not Found: " + fileName);
        }
    }

    public Double plainFun(Integer input) {
        double d = Double.valueOf(input);
        return Math.sin(d / 3.0);
    }

    public TryWrap<Integer> cube(Integer input) {
        return TryWrap.of(() -> (int) Math.pow(input, 3));
    }

    public TryWrap<Integer> sqBad(Integer it) {
        return TryWrap.of(() -> it * it / 0);
    }

    public TryWrap<Integer> sq(Integer it) {
        return TryWrap.of(() -> it * it);
    }
}
