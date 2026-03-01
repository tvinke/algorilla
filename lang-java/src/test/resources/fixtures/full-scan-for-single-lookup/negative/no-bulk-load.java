import java.util.*;
import java.util.stream.*;

public class ItemService {
    public Item getFirst(List<Item> items) {
        return items.stream()
            .filter(i -> i.isActive())
            .findFirst()
            .orElse(null);
    }
}
