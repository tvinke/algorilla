import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrderProcessor {
    public void processOrders(List<Order> orders) {
        for (Order order : orders) {
            CompletableFuture<Receipt> future = submitAsync(order);
            Receipt receipt = future.join();
            notifyCustomer(order, receipt);
        }
    }

    private CompletableFuture<Receipt> submitAsync(Order order) {
        return CompletableFuture.supplyAsync(() -> process(order));
    }

    private Receipt process(Order order) { return new Receipt(); }
    private void notifyCustomer(Order order, Receipt receipt) {}
}
