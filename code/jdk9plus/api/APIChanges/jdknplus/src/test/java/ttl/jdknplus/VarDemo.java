package ttl.jdknplus;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VarDemo {
	
	/**
	 * Pre Java 9 you *have* to declare the variables to be
	 * autoclosed in the try expression.  Now they can be set
	 * up outside, but *have* to be effectively final.
	 * @throws IOException
	 */
	@Test
	public void newTryWithResources() throws IOException {
		FileInputStream fis = new FileInputStream("abc");
		try(fis) {
			//Blah blah blah
		}
	}

	/**
	 * Java 10+.  Use of var
	 * - Works only with local variables
	 * - Variables have to be initialized
	 * - Can be used in for loops
	 *
     *   Can NOT be used for method arguments
	 * - Can NOT be set from initialized arrays
	 * - Can NOT be set from lambdas or method references
	 *
	 * See https://openjdk.java.net/projects/amber/LVTIstyle.html for usage
	 * guidelines.
	 */
	@Test
	public void typeInference() {
		var i = 10;
		var aString = "hello";

		assertTrue(aString instanceof String);

		//This will be a list of object
		var listObj = new ArrayList<>();
		listObj.add(2);
		listObj.add("abc");
		//Integer it = listObj.get(0);  Compile error
		assertTrue(listObj.get(0).getClass() == Integer.class);
		assertTrue(listObj.get(1).getClass() == String.class);
		//This will be a list of String

		var listStr = new ArrayList<String>();
		//listStr.add(2); Compile error
		listStr.add("abc");
		assertTrue(listStr.get(0).getClass() == String.class);

		//Can set to arrays
		var anArray = new String [10];

		//var initArray = {"abc", "def"};  Compile error
		//var func = (s) -> s + 5;   Compile error
	}

	public void cleanUpGenerics() {
		//Map where key = Student name and value = Map of Course name by Course mark
		Map<String, Map<String, Integer>> studentMarksPerCourse = new HashMap<>();

		Set<Map.Entry<String, Map<String, Integer>>> entries = studentMarksPerCourse.entrySet();

		var betterEntries = studentMarksPerCourse.entrySet();
	}

}
