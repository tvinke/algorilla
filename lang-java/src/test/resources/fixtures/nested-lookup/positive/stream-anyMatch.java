import java.util.List;

public class StreamAnyMatch {
    public long countMatching(List<String> items, List<String> targets) {
        return items.stream()
            .filter(item -> targets.contains(item))
            .count();
    }
}
