import java.util.HashMap;
import java.util.Map;

public class TypedLoopVarMapRemove {

    // Enhanced-for with typed loop variable: Map.Entry from HashMap
    // The loop variable 'entry' has type Map.Entry — remove() on the map is O(1)
    void removeExpiredEntries(HashMap<String, Long> cache, long threshold) {
        for (Map.Entry<String, Long> entry : cache.entrySet()) {
            if (entry.getValue() < threshold) {
                cache.remove(entry.getKey());
            }
        }
    }

    // Enhanced-for with typed String loop variable
    // String.contains() should be recognized as string op, not collection lookup
    boolean anyContainsKeyword(java.util.List<String> lines, String keyword) {
        for (String line : lines) {
            if (line.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
