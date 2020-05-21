package ttl.advjava.compfu;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Use a Completable Future to have to run work asynchronously and the gather
 * the results.
 *
 * @author whynot
 */
public class CompletableWorkers {

    private static ExecutorService executor;

    public static void main(String[] args) {
        executor = Executors.newFixedThreadPool(4);
        CompletableWorkers cw = new CompletableWorkers();
        //cw.thenAccepting();
        //cw.thenCombiningMoreThanTwo();
        //cw.thenCombining();
        //cw.thenApplying();
        //cw.thenComposing();
        cw.lotsOfFutures();

        cw.stop();
    }


    public void init() {
        // Create a fixed thread pool ExecutorService
        int numCpus = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(numCpus);
    }

    public void tearDown() {
        stop();
    }

    /**
     * Use thenAccept when you want to consume the result but not return anything.
     */
    public void thenAccepting() {
        CallableWorker w1 = new CallableWorker();
        CallableWorker w2 = new CallableWorker();

        // CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(() ->
        // w1.call());
        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(w1::call, executor);
        CompletableFuture<Integer> cf2 = CompletableFuture.supplyAsync(w2::call, executor);

        cf1.thenAcceptBothAsync(cf2, (r1, r2) -> {
            int finalResult = r1 + r2;
            System.out.println("Final Accept Result is " + finalResult);
        }, executor);

    }

    /**
     * Use thenCombine when you want to reduce the result of two independent
     * CompletableFutures
     */
    public void thenCombining() {
        CallableWorker w1 = new CallableWorker();
        CallableWorker w2 = new CallableWorker();
        BadWorker w3 = new BadWorker();

        CompletableFuture<Integer> cfi = new CompletableFuture<>();
        cfi.complete(10);

        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(w1::call, executor);
        CompletableFuture<Integer> cf2 = CompletableFuture.supplyAsync(w2::call, executor);

        CompletableFuture<Integer> ff = cf1.thenCombineAsync(cf2, (r1, r2) -> r1 + r2, executor);
        int finalResult = 0;
        // get v/s join in thenApplying
        // Checked v/s unchecked Exceptions
        try {
            finalResult = ff.get();
            System.out.println("Final Combined Result is " + finalResult);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    /**
     * Use thenCombine when you want to reduce the result of two independent
     * CompletableFutures
     */
    public void thenCombiningMoreThanTwo() {
        CallableWorker w1 = new CallableWorker();
        CallableWorker w2 = new CallableWorker();
        BadWorker w3 = new BadWorker();

        CompletableFuture<Integer> cfi = new CompletableFuture<>();
        cfi.complete(10);

        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(w1::call, executor);
        CompletableFuture<Integer> cf2 = CompletableFuture.supplyAsync(w2::call, executor);
        CompletableFuture<Integer> cf3 = CompletableFuture.supplyAsync(w2::call, executor);


        CompletableFuture<Integer> ff = cf1.thenCombineAsync(cf2, (r1, r2) -> r1 + r2, executor);
        CompletableFuture<Integer> ff2 = ff.thenCombineAsync(cf3, (r1, r2) -> r1 + r2, executor);

        int finalResult = 0;
        // get v/s join in thenApplying
        // Checked v/s unchecked Exceptions
        try {
            finalResult = ff2.get();
            System.out.println("Final Combined Result is " + finalResult);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    /**
     * Use thenApply to apply a Function to change the value and/or Type of the a
     * CompletableFuture
     */
    public void thenApplying() {
        CallableWorker w1 = new CallableWorker();
        CallableWorker w2 = new CallableWorker();
        BadWorker w3 = new BadWorker();

        // CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(() ->
        // w1.call());
        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(w1::call, executor);
        CompletableFuture<Integer> cf2 = CompletableFuture.supplyAsync(w2::call, executor);

        CompletableFuture<Integer> ff = cf1.thenCombineAsync(cf2, (r1, r2) -> r1 + r2, executor);


        // Change Type to String and value to whatever
        CompletableFuture<String> calculationOnFF = ff.thenApplyAsync(i -> {
            String s = "!" + (i * i) + "!";
            System.out.println("Intermediate Applied Result is " + i);
            return s;
        }, executor);
        // Unchecked CompletionException possible on the join()
        String finalResult = calculationOnFF.join();
        System.out.println("Final Applied Result is " + finalResult);
    }

    /**
     * Use thenCompose when a method returns a CompletableFuture. You then have to
     * extract the payload from that future to merge it with other data. That's what
     * thenCompose does - like a flatmap for CFs
     */
    public void thenComposing() {
        CallableWorker w1 = new CallableWorker();
        CallableWorker w2 = new CallableWorker();
        BadWorker w3 = new BadWorker();

		/*_
		// Call the first one, then call returnACompletableFuture which returns
		// a CompletableFuture that we need to flatten, so we can get at the payload in
		//it and merge that with the argument we will get which will be the result of the 
		//call to supplyAsync.  So we go from CompletableFuture<CompletableFuture<Integer>> to
		//CompletableFuture<Integer>.  We get that behavior from thenCompose.
		*/
        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(w1::call, executor)
                .thenCompose(this::returnACompletableFuture);

        CompletableFuture<CompletableFuture<Integer>> cf2 = CompletableFuture.supplyAsync(w1::call, executor)
                .thenApply(this::returnACompletableFuture);

        CompletableFuture<Integer> cf3 =
                CompletableFuture.supplyAsync(w1::call, executor)
                        .thenApply(this::returnAnInteger);


        List<Student> ls = new ArrayList<>();
        ls.add(new Student());
        ls.add(new Student());

        Stream<String> ss = ls.stream().flatMap(s -> s.phones.stream());


        // Unchecked CompletionException possible on the join()
        Integer finalResult = cf1.join();
        System.out.println("Final Composed Result is " + finalResult);
    }

    /**
     * Dealing with multiple CompletableFutures
     */
    public void lotsOfFutures() {
        List<CompletableFuture<String>> cfs = IntStream.range(0, 10).boxed().map(i -> {
            CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
                return LocalDateTime.now().toString() + " " + i;
            }, executor).thenApplyAsync(s -> {
                if(i % 2 == 0) {
                    String badGuy = null;
                    badGuy.length();
                }
                return s;
            }).exceptionally(t -> {
                System.out.println("Error happened: " + t);
                return null;
            });
            return cf;
        })
       .collect(Collectors.toList());


        CompletableFuture<?> last = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0]));
        last.join();

        List<CompletableFuture<String>> successful = cfs.stream()
                .filter(cf -> cf.join() != null)
                .collect(Collectors.toList());

        successful.forEach(cf -> System.out.println(cf.join()));
    }

    /**
     * Shutdown the ExecutorService
     */
    public void stop() {
        // Shutdown nicely
        executor.shutdown();

        int tryLimit = 5;
        int numTries = 0;
        // Spin for upto tryLimit times waiting for it to stop
        while (!executor.isTerminated()) {
            if (++numTries > tryLimit) {
                // Something is very wrong. Scream.
                System.err.println("Executor Service not terminated after " + numTries + " tries");
                return;
            }
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Use a Callable class to create a task Each Worker will do some calculation
     * and return a Result
     *
     * @author whynot
     */
    class CallableWorker implements Callable<Integer> {
        public Integer call() {
            int sum = 0;
            for (int i = 0; i < 1000; i++) {
                sum += i;
            }

            return sum;
        }
    }

    public Integer returnAnInteger(Integer input) {

        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += i;
        }
        return sum + input;
    }

    public CompletableFuture<Integer> returnACompletableFuture(Integer input) {

        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> {
            int sum = 0;
            for (int i = 0; i < 1000; i++) {
                sum += i;
            }
            return sum + input;
        });

        return cf;
    }

    /**
     * A Bad Worker. This one stays in a loop and doesn't listen for Interruption.
     * There is NO way to kill this Thread.
     *
     * @author whynot
     */
    class BadWorker implements Runnable {
        public void run() {
            int sum = 1;
            for (int i = 0; i <= sum; i++) {
                sum += i;
                i = sum - 1;
            }
        }
    }
}
