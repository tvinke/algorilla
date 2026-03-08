import java.util.*;
import java.util.stream.*;

public class SeparateChainsWithIf {
    public void process(List<Item> items, String id) {
        List<Item> sorted = items.stream().sorted(Comparator.comparing(Item::getName)).collect(Collectors.toList());
        if (id != null) {
            Item found = items.stream().filter(a -> a.getId().equals(id)).findAny().orElse(null);
        }
    }
}
