import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ConfigMerger {
    public Map<String, String> mergeConfigs(List<ConfigSource> sources) {
        Map<String, String> merged = new HashMap<>();
        for (ConfigSource source : sources) {
            merged.putAll(source.getProperties());
        }
        return merged;
    }
}
