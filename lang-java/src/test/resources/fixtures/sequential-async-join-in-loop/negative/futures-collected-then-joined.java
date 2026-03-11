import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class OrderBatchService {
    public void processOrdersBatch(List<Order> orders) {
        List<CompletableFuture<Receipt>> futures = new ArrayList<>();
        for (Order order : orders) {
            futures.add(submitAsync(order));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private CompletableFuture<Receipt> submitAsync(Order order) {
        return CompletableFuture.supplyAsync(() -> process(order));
    }

    private Receipt process(Order order) { return new Receipt(); }
}
