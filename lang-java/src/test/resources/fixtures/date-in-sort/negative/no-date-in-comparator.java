import java.util.*;

class ItemService {
    void sortByPrice(List<String> items) {
        items.sort((a, b) -> a.compareTo(b));
    }
}
