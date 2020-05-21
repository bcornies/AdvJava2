package ttl.trywrap;

/**
 * @author whynot
 */
public interface MyConsumer<T>{
    public void accept(T t) throws Exception;
}
