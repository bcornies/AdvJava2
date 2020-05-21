package ttl.advjava.compfu;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableGames {

    private ExecutorService eService = Executors.newFixedThreadPool(2);
	public static void main(String[] args) {
		//new CompletableGames().builtInPoolHasDaemonThreads();
		//System.out.println("All Done");
		CompletableGames cg = new CompletableGames();
		cg.doStuff();

	}


	public void runFuture() {
		//CompletableFuture<String> cs = new CompletableFuture<>();
		System.out.println(Thread.currentThread().getName());
		CompletableFuture<String> cs = doStuff();
		cs.thenAccept(str -> System.out.println("We got str"));

		CompletableFuture<String> cs2 = doStuff();
		cs2.thenAccept(str -> System.out.println("We got str " + str));
		//cs.join();

		eService.shutdown();

	}

	public CompletableFuture<String> doStuff() {
		CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
			System.out.println("Supply in " + Thread.currentThread().getName());
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			return "Hello";
		}, eService).thenApplyAsync(str -> {
			System.out.println("Apply in " + Thread.currentThread().getName());
			return str + " XXXX";
		}, eService);

		return cf;
	}

	public void lotsOfFutures() {

	}

	
	private ExecutorService es = Executors.newCachedThreadPool();
	
	/**
	 * If you create the CompletableFuture without the 
	 * 'es' argument, then you never see the output from the
	 * future because it will run in the common Thread Pool which
	 * has (*has* to have) daemon threads, so main finishes and the 
	 * VM exits
	 */
	public void builtInPoolHasDaemonThreads() {
		CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Future Returning 10");
			return 10;
		}, es);
	}

}
