package ttl.trywrap;

/**
 * @author whynot
 */
public interface MyPredicate<T> {
    public boolean test(T t) throws Exception;
}
