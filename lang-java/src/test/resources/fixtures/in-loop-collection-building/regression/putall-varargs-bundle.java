import java.util.*;

public class ConfigMerger {
    public Map<String, String> mergeConfigs(List<Map<String, String>> sources) {
        Map<String, String> merged = new HashMap<>();
        for (Map<String, String> source : sources) {
            merged.putAll(source);
        }
        return merged;
    }
}
