import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredicateMatchesRegression {

    // Predicate.matches() is not a regex operation — should not fire
    void filterWithPredicate(List<String> items, Predicate<String> predicate) {
        for (String item : items) {
            if (predicate.matches(item)) {
                System.out.println(item);
            }
        }
    }

    // Pattern.matches() uses an already-compiled pattern — should not fire
    void matchWithPattern(List<String> items, Pattern pattern) {
        for (String item : items) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.matches()) {
                System.out.println(item);
            }
        }
    }

    // Name heuristic: variable named "predicate" — should not fire
    void filterWithNamedPredicate(List<String> items) {
        Object predicate = getPredicate();
        for (String item : items) {
            if (predicate.matches(item)) {
                System.out.println(item);
            }
        }
    }

    private Object getPredicate() { return null; }
}
