import java.util.*;

public class HashMapConstructorRemove {
    public void purgeExpired(List<String> data, List<String> expired) {
        Map<String, String> cache = new HashMap<>();
        for (String d : data) {
            cache.put(d, d);
        }
        for (String key : expired) {
            cache.remove(key);
        }
    }
}
