import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentCacheCleaner {
    private final ConcurrentHashMap<String, Object> sessions = new ConcurrentHashMap<>();

    public void evict(List<String> staleKeys) {
        for (String key : staleKeys) {
            sessions.remove(key);
        }
    }
}
