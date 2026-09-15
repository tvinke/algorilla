import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class MapEntryUnpackingCopiedValue {
    List<String> flattenMap(Map<String, List<String>> grouped) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            List<String> values = entry.getValue();
            for (String value : values) {
                result.add(value);
            }
        }
        return result;
    }
}
