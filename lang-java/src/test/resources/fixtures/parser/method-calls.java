import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MethodCallExamples {
    public void lookupCalls(List<String> items, Map<String, Integer> map) {
        items.contains("test");
        items.indexOf("test");
        map.containsKey("key");
        map.containsValue(42);
    }

    public void sortCalls(List<String> items) {
        items.sort(null);
        items.stream().sorted();
    }

    public void accessCalls(List<String> items) {
        items.stream().findFirst();
        items.stream().findAny();
    }

    public void chainedCalls(List<String> items) {
        items.stream().filter(s -> s.length() > 3).findFirst();
    }

    public void objectCreation() {
        HashMap<String, Integer> map = new HashMap<>();
        Object obj = new Object();
    }
}
