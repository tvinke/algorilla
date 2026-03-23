import java.util.List;
import java.util.ArrayList;

public class EnumOuterLoop {
    enum Priority { HIGH, MEDIUM, LOW }

    // Outer loop iterates enum values — constant-bound.
    // Inner loop mutates a collection, but total work is O(k*n) not O(n*m).
    List<String> categorize(List<String> tasks) {
        List<String> result = new ArrayList<>();
        for (Priority p : Priority.values()) {
            for (String task : tasks) {
                result.add(p.name() + ": " + task);
            }
        }
        return result;
    }
}
