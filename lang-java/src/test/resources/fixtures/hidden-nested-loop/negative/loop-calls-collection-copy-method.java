import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class BatchCollector {
    public List<String> collectAll(List<List<String>> groups) {
        List<String> result = new ArrayList<>();
        for (List<String> group : groups) {
            result.addAll(group);
        }
        return result;
    }

    public Map<String, String> mergeAll(List<Map<String, String>> maps) {
        Map<String, String> merged = new HashMap<>();
        for (Map<String, String> map : maps) {
            merged.putAll(map);
        }
        return merged;
    }
}
