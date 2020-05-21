package ttl.jdknplus;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

/**
 * Code examples to illustrate changes to the Stream API from jdk 9 onwards
 *
 * @author whynot
 */
public class StreamChanges {

	/**
	 * takeWhile and dropWhile
	 * Jdk9
	 */
	@Test
	public void takeWhileDropWhileDemo() {
		IntStream.iterate(0, (i -> i + 1))
				.dropWhile(i -> i < 10)
				.takeWhile(i -> i < 20)
				.forEach(System.out::println);
	}

	/**
	 * Can use the Predicate version of iterate to control when the Loop ends,
	 * instead of takeWhile like above
	 */
	@Test
	public void iterateWithPredicate() {
		IntStream.iterate(0, (i) -> i < 20, (i -> i + 1))
				.dropWhile(i -> i < 10)
				.forEach(System.out::println);
	}

}
