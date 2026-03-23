import java.util.*;

class SortThenArbitraryIndex {
    String pickNth(List<String> items, int n) {
        Collections.sort(items);
        return items.get(n);
    }
}
