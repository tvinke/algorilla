import java.util.List;
import java.util.stream.Collectors;

public class CollectionStreamNotIO {
    // Collection.stream() is an in-memory operation, not IO.
    // Should NOT be flagged as io-in-loop.

    List<String> processItems(List<List<String>> batches) {
        List<String> results = new java.util.ArrayList<>();
        for (List<String> batch : batches) {
            List<String> filtered = batch.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            results.addAll(filtered);
        }
        return results;
    }
}
