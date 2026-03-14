import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CacheMapTargetInLoop {
    public void lookupFromCache(List<String> keys) {
        Map<String, Object> localCache = new HashMap<>();
        for (String key : keys) {
            if (localCache.contains(key)) {
                System.out.println("hit");
            }
        }
    }

    public void lookupFromMap(List<String> keys, Map<String, Object> userMap) {
        for (String key : keys) {
            if (userMap.contains(key)) {
                System.out.println("hit");
            }
        }
    }

    public void lookupFromIndex(List<String> keys) {
        Map<String, Integer> nameIndex = new HashMap<>();
        for (String key : keys) {
            if (nameIndex.contains(key)) {
                System.out.println("hit");
            }
        }
    }
}
