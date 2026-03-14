import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class MapEntryUnpacking {
    List<String> flattenMap(Map<String, List<String>> grouped) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            for (String value : entry.getValue()) {
                result.add(value);
            }
        }
        return result;
    }
}
