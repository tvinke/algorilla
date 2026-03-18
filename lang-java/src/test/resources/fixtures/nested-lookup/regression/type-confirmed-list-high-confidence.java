import java.util.List;
import java.util.ArrayList;

public class TypeConfirmedListHighConfidence {

    /**
     * TypeEnvironment will confirm 'targets' is a List (declared parameter type).
     * Finding should be HIGH confidence because the type proves O(n) lookup.
     */
    public void findMatches(List<String> items, List<String> targets) {
        for (String item : items) {
            if (targets.contains(item)) {
                System.out.println(item);
            }
        }
    }

    /**
     * TypeEnvironment will confirm 'lookup' is an ArrayList (constructor inference).
     * Finding should be HIGH confidence.
     */
    public void findWithArrayList(List<String> items) {
        ArrayList<String> lookup = new ArrayList<>();
        lookup.add("a");
        for (String item : items) {
            if (lookup.contains(item)) {
                System.out.println(item);
            }
        }
    }
}
