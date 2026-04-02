import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class MapGetPutNotIo {
    private final Map<String, Set<String>> linkedResources = new HashMap<>();

    void mergeLinks(Map<String, Set<String>> incoming) {
        for (Entry<String, Set<String>> linkentry : incoming.entrySet()) {
            Set<String> linkedIdSet = linkedResources.get(linkentry.getKey());
            if (linkedIdSet != null) {
                linkedIdSet.addAll(linkentry.getValue());
            } else {
                linkedResources.put(linkentry.getKey(), linkentry.getValue());
            }
        }
    }
}
