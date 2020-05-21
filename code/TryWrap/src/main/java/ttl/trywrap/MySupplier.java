package ttl.trywrap;

/**
 * @author whynot
 */
public interface MySupplier<R>{
    public R get() throws Exception;
}
