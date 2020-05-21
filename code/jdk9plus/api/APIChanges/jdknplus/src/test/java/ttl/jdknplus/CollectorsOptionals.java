package ttl.jdknplus;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class CollectorsOptionals {

	static class Album {
		public Double price;
		public List<String> artists;

		public Album(double price, String... artists) {
			this.price = price;
			this.artists = List.of(artists);
		}

		@Override
		public String toString() {
			return "Album [price=" + price + ", artists=" + artists + "]";
		}
	}

	public static Stream<Arguments> initAlbumsWithStream() {
        Stream<Arguments> stream =
                Stream.of(
                        arguments(
                        		List.of(new Album(22.5, "Rod", "Brackets"),
                        				new Album(333.80, "Coil", "Wires", "Copper"),
                        				new Album(333.80, "Copper", "Glass")
                        				)
                        		)
                        );

        return stream;
    }

	public static List<List<Album>> initAlbumsWithList() {
		List<Album> albums = List.of(new Album(22.5, "Rod", "Brackets"),
                        				new Album(333.80, "Gayle", "Jimi", "Miles"),
                        				new Album(333.80, "William", "Betty")
                        				);
                        		

        return List.of(albums);
    }

	/**
	 * Collectors.filtering
	 */
	//@formatter:off
	@ParameterizedTest
	@MethodSource("initAlbumsWithList")
	public void collectorsFiltering(List<Album> albums) {
		//Get a Map of <List of Artists> by Set<Albums> but only if 
		//the Album for that list of Artists has a price greater than 100
		Map<List<String>, Set<Album>> compsByPrice = 
				albums.stream().collect(Collectors.groupingBy(b -> b.artists,
				//We want only the Albums for which price is greater than 
				//100, else we get an empty set
				Collectors.filtering(b -> b.price > 100, Collectors.toSet())));
		compsByPrice.forEach((k, v) -> System.out.println(k + " : " + v));
	}
	/**
	 * Collectors.flatMapping
	 */
	@ParameterizedTest
	@MethodSource("initAlbumsWithList")
	public void collectorsFlatMapping(List<Album> albums) {
		//Map of Price to a Set of all the artists for all the albums selling at that
		//price
		Map<Double, Set<String>> compsByPrice = albums.stream().collect(Collectors.groupingBy(b -> b.price,
				// We need the flatMap here because we have to
				// get at the individual artists in the List of
				// artists. Only way to do that is to stream
				// the array and then flatMap it
				Collectors.flatMapping(b -> b.artists.stream(), Collectors.toSet())));
		compsByPrice.forEach((k, v) -> System.out.println(k + " : " + v));
	}

	/**
	 * Optional.stream
	 */
	@ParameterizedTest
	@MethodSource("initAlbumsWithList")
	public void optionalToStream(List<Album> albums) {
		//We want a list of the first artist of every album
		List<String> ss = albums.stream()
				//This is tricky.  We are doing a findFirst of the
				//inner Stream, so what we get out of the map is
				//a Stream of Optionals, each of which will either
				//be empty or have a value
				.map(album -> album.artists.stream().findFirst())
				//We stream the Optional and then flatMap that stream.
				//flatMap will ignore the empty Optionals and give
				//us the values of the full ones
				.flatMap(opt -> opt.stream())
				.collect(Collectors.toList());

		System.out.println(ss);
	}

	/**
	 * Above example with Java 8.  No flatMap on Optional
	 * so we have to resort to filtering
	 */
	@ParameterizedTest
	@MethodSource("initAlbumsWithList")
	public void optionalToStreamJava8(List<Album> albums) {
		//We want a list of the first artist of every album
		List<String> ss = albums.stream()
				//This is tricky.  We are doing a findFirst of the
				//inner Stream, so what we get out of the map is
				//a Stream of Optionals, each of which will either
				//be empty or have a value
				.map(album -> album.artists.stream().findFirst())
				.filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
				.collect(Collectors.toList());

		System.out.println(ss);
	}

	//@formatter:on

}
