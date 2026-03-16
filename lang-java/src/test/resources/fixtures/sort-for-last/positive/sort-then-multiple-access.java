import java.util.*;
import java.util.stream.*;

class SortThenMultipleAccess {
    String findBest(List<String> items) {
        items.sort(Comparator.naturalOrder());
        String first = items.get(0);
        return first;
    }
}
