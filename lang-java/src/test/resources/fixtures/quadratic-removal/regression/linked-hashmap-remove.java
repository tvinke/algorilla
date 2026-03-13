import java.util.*;

public class LruCacheCleanup {
    public void removeOldEntries(LinkedHashMap<String, String> lruCache, List<String> evicted) {
        for (String key : evicted) {
            lruCache.remove(key);
        }
    }
}
