package ttl.trywrap;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author whynot
 */
public class TryWrap<T> {
    private T right;
    private Exception left;
    private boolean empty = false;

    /**
     * Wrap either the result or an Exception in a TryWrap
     *
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> TryWrap<T> of(MySupplier<T> supplier) {
        try {
            T t = supplier.get();
            return ofRight(t);
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    /**
     * Wrap any Exceptions in a TryWrap
     * This will create an empty TryWrap.
     * Note that the left will be set to throw
     * NoSuchElementException if an attempt is
     * made to retrieve the value from an Empty
     *
     * @param runnable
     * @param <T>
     * @return
     */
    public static <T> TryWrap<T> ofRunnable(MyRunnable runnable) {
        try {
            runnable.run();
            return ofEmpty();
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    /**
     * Create an Empty TryWrap.  A NoSuchElementException
     * will be thrown on any attempt retrieve a value from
     * an empty
     * @param <T>
     * @return
     */
    public static <T> TryWrap<T> ofEmpty() {
        TryWrap<T> me = new TryWrap<>(null, null);
        me.empty = true;
        me.left = new NoSuchElementException("Empty TryWrap");
        return me;
    }

    public static <T> TryWrap<T> ofRight(T x) {
        TryWrap<T> me = new TryWrap<>(null, x);
        return me;
    }

    public static <T> TryWrap<T> ofLeft(Exception y) {
        TryWrap<T> me = new TryWrap<>(y, null);
        return me;
    }

    public T right() {
        if (right == null) {
            throw new NoSuchElementException("Null or Empty TryWrap");
        }
        return right;
    }

    public Exception left() {
        if(left == null) {
            throw new NoSuchElementException("left Element is null");
        }
        return left;
    }

    public boolean isEmpty() {
        return empty == true;
    }

    public boolean isRight() {
        return right != null;
    }

    public boolean isLeft() {
        return !isEmpty() && left != null;
    }

    /**
     * If we don't have a right and we are empty, return an empty.
     * If we don't have right and we are not empty, then we *have*
     * to have a left, so return that.
     *
     * If we have a right, call the function on it.  If all goes well,
     * return a TryWrap with the new value.  Else return a TryWrap with
     * the Exception
     * @param function
     * @param <R>
     * @return A TryWrap created according to the above rules
     */
    public <R> TryWrap<R> map(MyFunction<? super T, ? extends R> function) {
        assert(right != null || left != null | isEmpty());
        if (right == null) {
            if (isEmpty()) {
                return ofEmpty();
            }
            return ofLeft(left);
        }
        try {
            R r = function.apply(right);
            return ofRight(r);
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    /**
     * If we don't have a right and we are empty, return an empty.
     * If we don't have right and we are not empty, then we *have*
     * to have a left, so return that.
     *
     * If we have a right, call the function on it.  If all goes well,
     * return the TryWrap that the function returns.  Else return a TryWrap
     * with the Exception
     * @param function
     * @param <R>
     * @return A TryWrap created according to the above rules
     */
    public <R> TryWrap<R> flatMap(MyFunction<? super T, TryWrap<R>> function) {
        assert(right != null || left != null | isEmpty());
        if (right == null) {
            if (isEmpty()) {
                return ofEmpty();
            }
            return ofLeft(left);
        }
        try {
            TryWrap<R> r = function.apply(right);
            return r;
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    /**
     * If we don't have a right and we are empty, return an empty.
     * If we don't have right and we are not empty, then we *have*
     * to have a left, so return that.
     *
     * If we have a right, call the predicate function on it.
     * If all goes well, return ourself (this).  Else return a TryWrap
     * with the Exception
     * @param predicate
     * @return A TryWrap created according to the above rules
     */
    public TryWrap<T> filter(MyPredicate<? super T> predicate) {
        assert(right != null || left != null | isEmpty());
        if (right == null) {
            if (isEmpty()) {
                return ofEmpty();
            }
            return ofLeft(left);
        }
        boolean allOk = false;
        try {
            boolean r = predicate.test(right);
            if (r) {
                return this;
            } else {
                return ofEmpty();
            }
        } catch (Exception e) {
            return ofLeft(e);
        }
    }

    /**
     * Call the consumer if we have a value.
     * Any Exceptions thrown from the consumer are
     * rethrown as RuntimeExceptions
     * @param consumer
     * @throws RuntimeException
     */
    public void ifPresent(MyConsumer<T> consumer) {
        assert(right != null || left != null | isEmpty());
        if(isRight()) {
            try {
                consumer.accept(right);
            }catch (Exception e) {
                right = null;
                orElseThrowUnchecked(e);
            }
        }
    }

    /**
     * If we don't have a right, call the consumer with
     * our Exception.  Useful if you want to examine and
     * maybe log the Exception rather than throw it.
     * Any Exception thrown by the consumer will be rethrown
     * as a RuntimeException
     * @param consumer
     * @throws RuntimeException
     */
    public void orElseConsume(MyConsumer<Exception> consumer) {
        assert(right != null || left != null | isEmpty());
        if (isLeft()) {
            try {
                consumer.accept(left);
            }catch (Exception e) {
                right = null;
                orElseThrowUnchecked(e);
            }
        }
    }


    /**
     * Go either way.
     *
     * @param successConsumer
     * @param exConsumer
     */
    public void ifPresentOrElseConsume(MyConsumer<T> successConsumer, MyConsumer<Exception> exConsumer) {
       if(isRight()) {
           ifPresent(successConsumer);
       }
       else {
           orElseConsume(exConsumer);
       }
    }

    /**
     * Return our right or the given default value.
     * This will disregard any Exception
     *
     * @param t The value to return if we are a left
     * @return Our right or t
     */
    public T orElse(T t) {
        assert(right != null || left != null | isEmpty());
        if(isRight()) {
            return right;
        }
        return t;
    }

    /**
     * Return our right or null, if you really really want it.
     * Your functional colleagues might sniff.  But never say never.
     *
     * @return Our right or null
     */
    public T orElseNull() {
        assert(right != null || left != null | isEmpty());
        if(isRight()) {
            return right;
        }
        return null;
    }

    /**
     * Throw our exception if we don't have a value.
     * Hopefully our invariants are intact, which means
     * if we don't have a right, we *have* to have a left.
     * Will Throw NoSuchElementException if called on an
     * empty.
     *
     * @return
     * @throws Exception
     */
    public T orElseThrow() throws Exception {
        assert(right != null || left != null | isEmpty());
        if (isRight()) {
            return right;
        }
        throw left;
    }

    /**
     * Throw our exception unchecked
     * @return
     */
    public T orElseThrowUnchecked() {
        assert(right != null || left != null | isEmpty());
        if (isRight()) {
            return right;
        }
        if(RuntimeException.class.isAssignableFrom(left.getClass())) {
            throw (RuntimeException)left;
        }
        throw new RuntimeException(left);
    }


    /**
     * If we don't have a right, throw the given Exception
     *
     * @param e The Exception to throw
     * @return  Our right, or throw the given Exception
     * @throws Exception
     */
    public T orElseThrow(Exception e) throws Exception {
        if (isRight()) {
            return right;
        }
        throw e;
    }

    /**
     * If we don't have a right, throw the give Exception unchecked
     * @param e
     * @return Our right, or throw given Exception unchecked
     */
    public T orElseThrowUnchecked(Exception e) {
        if (isRight()) {
            return right;
        }
        throw new RuntimeException(e);
    }

    public void orElsePrintStackTrace() {
        if (isLeft()) {
            left.printStackTrace();
        }
    }


    private TryWrap(Exception left, T right) {
        this.right = right;
        this.left = left;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TryWrap<?> tryWrap = (TryWrap<?>) o;
        return empty == tryWrap.empty &&
                Objects.equals(right, tryWrap.right) &&
                Objects.equals(left, tryWrap.left);
    }

    @Override
    public int hashCode() {
        return Objects.hash(right, left, empty);
    }
}
