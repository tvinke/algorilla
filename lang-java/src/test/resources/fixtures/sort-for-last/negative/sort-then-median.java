import java.util.*;

class SortThenMedian {
    int findMedian(List<Integer> values) {
        Collections.sort(values);
        int medianIndex = values.size() / 2;
        return values.get(medianIndex);
    }
}
