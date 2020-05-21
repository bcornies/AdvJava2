package ttl.jdknplus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.Scanner;

/**
 * Information of and control over processes running on the machine
 */
public class ProcessHandleDemo {

	/**
	 * A 'ps' command.
	 */
	@Test
	public void ourPs() {
		ProcessHandle.allProcesses()
			.filter(ph -> ph.info().commandLine().isPresent() && ph.info().startInstant().isPresent())
			.forEach(ph -> {
				ProcessHandle.Info pInfo = ph.info();
				long pid = ph.pid();
				String command = pInfo.commandLine().get();
				Instant startInstant = pInfo.startInstant().get();
				System.out.println(pid + "    " + startInstant + "    " + command);
			});
	}

	/**
	 * Kill a process.  This one is slightly dangerous because it
	 * searches a process command line for the given string
	 * and kills the first one it finds.  Enter an actual
	 * process name in the ValueSource to work the magic.
	 *
	 * @param searchString
	 */
	@ParameterizedTest
	@ValueSource(strings = {"Enter an actual process name here"})
	public void killAProcess(String searchString) {
		ProcessHandle.allProcesses()
			.filter(ph -> ph.info().commandLine()
					.map(com -> com.contains(searchString))
					.orElse(false))
			.findFirst()
			.ifPresentOrElse(ph -> {
				ph.onExit().thenAccept(kph -> System.out.println("Killed: " + kph.pid()));

				System.out.println("Found process " + ph.pid() + ": " + ph);
				System.out.print("Kill (y/n)? ");
				String yn = new Scanner(System.in).nextLine();
				if(yn.equals("y")) {
					ph.destroy();
				}
				
				
				ph.onExit().join();

			}, 
			() -> {
				System.out.println("searchString not found: " + searchString);
			});

	}

	/**
	 * Launch a process to run a bash script to do
	 * ls /tmp | wc -l
	 */
	@Test
	public void launchAProcess() {

	}

}
