import java.util.List;
import java.util.stream.Collectors;

public class ParallelCollectSafe {
    public List<String> process(List<String> items) {
        return items.parallelStream()
            .map(item -> item.toUpperCase())
            .collect(Collectors.toList());
    }
}
