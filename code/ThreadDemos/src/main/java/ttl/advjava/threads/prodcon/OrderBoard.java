package ttl.advjava.threads.prodcon;

public interface OrderBoard {
    void postOrder(Order toBeProcessed);

    Order cookOrder();
}
