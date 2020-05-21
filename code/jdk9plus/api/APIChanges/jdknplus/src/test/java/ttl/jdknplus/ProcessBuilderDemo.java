package ttl.jdknplus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 * Run the given unix command.
 */
public class ProcessBuilderDemo {

	public static void main(String[] args) throws IOException {
		args = new String[] { "bash", "-c", "echo $PATH" };
		List<String> result = runCommand(args);
		System.out.println("result: " + result);
	}

	public static List<String> runCommand(String [] args) throws IOException {
	    if(args.length == 0) {
			//args = new String[]{"ls", "-axF", "/tmp"};
			args = new String[] { "bash", "-c", "echo $PATH" };
		}

		ProcessBuilder processBuilder = new ProcessBuilder(args);

	    //To get and set environment variables
		Map<String, String> env = processBuilder.environment();
		String path = env.get("PATH");
		System.out.println("Current Path is " + path);

		env.remove("JUNK");
		env.put("PATH", path + ":/dummy");

		processBuilder.directory(new File("/tmp"));

		// Send stderr to stdout
		processBuilder.redirectErrorStream(true);
		
		Process process = processBuilder.start();
		
		Scanner scanner = new Scanner(process.getInputStream());
		List<String> output = new ArrayList<>();
		
		System.out.printf("Output of running %s is:", Arrays.toString(args));

		String line;
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			output.add(line);
			System.out.println(line);
		}
		scanner.close();
		return output;
	}
}
