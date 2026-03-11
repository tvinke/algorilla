import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BatchProcessor {
    public void processBatch(List<Item> items) {
        for (Item item : items) {
            CompletableFuture<Result> asyncTask = validateAsync(item);
            Result result = asyncTask.join();
            store(result);
        }
    }

    private CompletableFuture<Result> validateAsync(Item item) {
        return CompletableFuture.supplyAsync(() -> validate(item));
    }

    private Result validate(Item item) { return new Result(); }
    private void store(Result result) {}
}
