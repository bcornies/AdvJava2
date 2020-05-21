package ttl.trywrap;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author whynot
 */
public class TestTryWrapNullPointer {

    class MyClass
    {
        String s;
    }
    @Test
    public void testNullPointer() {

        MyClass mc = new MyClass();

        Predicate<String> pred = str -> mc.s.contains(str);

        boolean result = TryWrap.of(() -> pred.test("boo")).orElse(false);

        boolean r = false;
        if(mc.s != null) {
           r = pred.test("boo");
        }

        boolean result2 = mc.s == null ? false : pred.test("boo");

        assertFalse(result);
    }
}
