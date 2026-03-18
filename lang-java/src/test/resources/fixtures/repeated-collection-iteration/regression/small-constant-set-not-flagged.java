import java.util.Set;
import java.util.stream.Collectors;

public class SmallConstantSet {
    // Iterating a 3-element constant set twice is negligible overhead
    public void processTypes(List<Item> items) {
        Set<String> types = Set.of("A", "B", "C");
        long count = types.stream().filter(t -> isActive(t)).count();
        List<String> labels = types.stream().map(t -> t.toLowerCase()).collect(Collectors.toList());
    }

    private boolean isActive(String type) { return true; }
}
