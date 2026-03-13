import java.util.*;

public class BulkOpsInLoop {
    // clear(), removeAll(), retainAll(), removeIf() are bulk operations — O(n) per call
    // but NOT per-element shifting. They should not trigger quadratic-removal.
    public void resetBatchBuffers(List<List<String>> batches) {
        for (List<String> batch : batches) {
            batch.clear();
        }
    }

    public void removeStaleEntries(List<String> items, Set<String> stale) {
        for (int i = 0; i < 10; i++) {
            items.removeAll(stale);
        }
    }

    public void filterByRetain(List<String> items, Set<String> allowed) {
        for (int i = 0; i < 10; i++) {
            items.retainAll(allowed);
        }
    }

    public void conditionalRemove(List<String> items) {
        for (int i = 0; i < 10; i++) {
            items.removeIf(s -> s.isEmpty());
        }
    }
}
