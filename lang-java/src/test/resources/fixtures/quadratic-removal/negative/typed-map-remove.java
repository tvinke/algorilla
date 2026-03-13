import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TypedMapCleaner {
    public void removeExpiredHeaders(Map<String, String> headers, List<String> expired) {
        for (String key : expired) {
            headers.remove(key);
        }
    }

    public void cleanRegistry(ConcurrentHashMap<String, Object> registry, Set<String> stale) {
        for (String key : stale) {
            registry.remove(key);
        }
    }

    public void pruneParameters(HashMap<String, String> parameters, List<String> unused) {
        for (String param : unused) {
            parameters.remove(param);
        }
    }
}
