import java.util.*;

public class MapProcessor {
    public void normalizeValues(Map<String, String> config) {
        for (int i = 0; i < 3; i++) {
            config.replaceAll((key, value) -> value.trim());
        }
    }

    public void uppercaseEntries(HashMap<String, String> headers) {
        for (String prefix : List.of("X-", "Content-")) {
            headers.replaceAll((k, v) -> k.startsWith(prefix) ? v.toUpperCase() : v);
        }
    }
}
