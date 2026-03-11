import java.util.*;

public class MapCleaner {
    public void removeKeys(Map<String, String> map, List<String> keys) {
        for (String key : keys) {
            map.remove(key);
        }
    }
}
