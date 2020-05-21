package ttl.advjava.threads.examples;

import org.junit.jupiter.api.Test;
import ttl.advjava.threads.racecondition.RaceCondition;
import ttl.advjava.threads.racecondition.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Program to illustrate a race condition. We create a bunch of threads which
 * are accessing a shared Repository of data. The threads spin in a loop and
 * increment a counter in the repository. We want the counter to always have a
 * coherent value, which is one more than it's last value. So, if we have 5
 * threads that spin around 50 times each, we want the final value of the
 * counter to be 249.
 * <p/>
 *
 * @author Anil Pal
 */
public class TestRaceCondition {

    private static final int numRacers = 5;

    // To keep track of the number of Threads
    private static List<RaceCondition> racers = new ArrayList<>();



    @Test
    public void testThatNoDuplicatesAreProduced() {
        Instant start = Instant.now();
        Repository rep = new Repository();
        for (int i = 0; i < numRacers; i++) {
            RaceCondition rc = new RaceCondition(i + 1 + "", rep);
            racers.add(rc);
            rc.start();
        }


        // Collect all the numbers into one list
        List<Integer> allNums = new ArrayList<Integer>();
        for (RaceCondition rc : racers) {
            try {
                rc.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            allNums.addAll(rc.getNumbers());
        }

        long dur = Duration.between(start, Instant.now()).toMillis();
        System.out.println("Racer took (ms): " + dur);

        // Look for duplicates. If all has gone well, there should
        // not be any duplicates.
        Collections.sort(allNums);
        System.out.println("Duplicates: ");
        Integer last = null;
        int end = allNums.size();
        List<Integer> dups = new ArrayList<>();

        for (int i = 0; i < end; i++) {
            Integer curr = allNums.get(i);
            if (curr.equals(last)) {
                dups.add(curr);
                System.out.print(last + " ");
            }
            last = curr;
        }

        // Look for duplicates. If all has gone well, there should
        // not be any duplicates.
        //List<Integer> dups = allNums.stream().distinct().collect(toList());
//        Set<Integer> nonDups = new HashSet<>();
//        Set<Integer> dups = allNums.stream()
//                .filter(i -> !nonDups.add(i))
//                .collect(Collectors.toSet());
//        System.out.println("Duplicates: ");
//        System.out.println(dups);

        assertEquals(0, dups.size());
    }
}
