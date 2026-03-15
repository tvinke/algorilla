import java.util.*;

public class MultiCollectionSort {
    // Sort one list, access first element of a different list — not a sort-for-first pattern
    public void processData(List<String> items, List<Integer> indices) {
        Collections.sort(items);
        int firstIndex = indices.get(0);
        System.out.println(firstIndex);
    }
}
