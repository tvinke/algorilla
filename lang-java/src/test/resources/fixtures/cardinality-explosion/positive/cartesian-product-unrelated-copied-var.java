import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class CartesianProductUnrelatedCopiedVar {
    List<Pair> crossJoin(Map<String, List<Item>> grouped, ItemService otherService) {
        List<Pair> result = new ArrayList<>();
        for (Map.Entry<String, List<Item>> entry : grouped.entrySet()) {
            // Not entry.getValue() - an unrelated collection that just happens to be named
            // "values", the same way a real map-value copy would be.
            List<Item> values = otherService.fetchAll();
            for (Item item : values) {
                result.add(new Pair(entry.getKey(), item));
            }
        }
        return result;
    }
}
