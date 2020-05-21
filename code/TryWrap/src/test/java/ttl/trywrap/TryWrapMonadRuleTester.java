package ttl.trywrap;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test Monad Laws.  Adapted from:
 * https://github.com/afcastano/JavaMonadsExample/blob/master/test/afcastano/monads/result/ResultMonadLawsTest.java
 *
 * @author whynot
 */
public class TryWrapMonadRuleTester {


    /**
     * Left Identity.
     * - Create the monad with some value a, and flatMap with some function f.
     * - The result should be the same as simple calling the f with a
     *
     * i.e. the flatMap should have the same effect as having called the function with
     * the raw value.  i.e. the Monad is doing it's job in reaching out and working on the
     * real value
     */
    @Test //unit(a) flatMap f === f(a)
    public void leftIdentity() throws Exception {
        int val = 2;
        assertThat(TryWrap.of(() -> {return val;}).flatMap(f), is(f.apply(val)));
        TryWrap<Integer> m1 = TryWrap.of(() -> val);
        assertThat(m1.flatMap(f), is(f.apply(val)));
    }


    /**
     * Right Identity
     * - Create a Monad and flatMap it with a 'unit' function that returns a Monad
     *   with it's argument.  i.e. it simply wraps a Monad around whatever is passed
     *   in.
     *
     * - The result should be the same as the original Monad.
     */
    @Test //m flatMap unit === m
    public void rightIdentity() throws Throwable {
        TryWrap<Integer> m1 = TryWrap.of(() -> 2);
        TryWrap<Integer> m2 = m1.flatMap(unitiser);
        assertThat(m1.flatMap(unitiser), is(m1));
    }

    /**
     * Associativity
     * - Case 1: Create a Monad from some raw value v.  flatMap it with some
     *   function f.  Flat map the resulting Monad with g.  This will be the final result.
     * - Case 2: Apply the function f to the raw value v directly.  flatMap the resulting
     *   Monad with g.  This will be the final result of the call to result.flatMap.
     *
     *   You should end up with the same (as in equal) Monad in both cases.
     */
    @Test //(m flatMap f) flatMap g === m flatMap ( f(x) flatMap g )
    public void associativity() {
        int v = 2;
        TryWrap<Integer> result = TryWrap.of(() -> v);
        //Case 1
        TryWrap<String> first = (result.flatMap(f)).flatMap(g);
        //Case 2
        TryWrap<String> second = result.flatMap(val -> f.apply(val).flatMap(g));

        assertThat(first, is(second));
    }

    //For left identity - normal flatMapper
    private MyFunction<Integer, TryWrap<String>> f =
            (i -> TryWrap.of(() -> "" + i));

    //For associativity
    private MyFunction<String, TryWrap<String>> g =
            (s -> TryWrap.of(() -> "[" + s + "]"));

    //For Right identity - returns a Monad wrapping the argument.
    private MyFunction<Integer, TryWrap<Integer>> unitiser =
            (i -> TryWrap.of(() -> i));

}
