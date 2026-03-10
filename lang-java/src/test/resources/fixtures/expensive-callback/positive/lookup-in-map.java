import java.util.List;
import java.util.stream.Collectors;

public class LookupInMap {
    public List<Boolean> checkPresence(List<String> items, List<String> allowed) {
        return items.stream()
            .map(item -> allowed.contains(item))
            .collect(Collectors.toList());
    }
}
