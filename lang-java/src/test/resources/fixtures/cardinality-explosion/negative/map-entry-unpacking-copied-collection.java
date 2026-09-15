import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class MapEntryUnpackingCopiedCollection {
    List<String> flattenMap(Map<String, List<String>> grouped) {
        List<String> result = new ArrayList<>();
        Set<Map.Entry<String, List<String>>> entries = grouped.entrySet();
        for (Map.Entry<String, List<String>> entry : entries) {
            for (String value : entry.getValue()) {
                result.add(value);
            }
        }
        return result;
    }
}
