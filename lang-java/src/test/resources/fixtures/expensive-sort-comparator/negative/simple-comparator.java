import java.util.*;

class ItemService {
    void sortByName(List<String> items) {
        items.sort((a, b) -> a.compareTo(b));
    }
}
