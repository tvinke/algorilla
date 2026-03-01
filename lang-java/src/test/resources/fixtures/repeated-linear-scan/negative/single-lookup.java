import java.util.*;
import java.util.stream.*;

public class ItemService {
    public Item findItem(List<Item> items, String id) {
        return items.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }
}
