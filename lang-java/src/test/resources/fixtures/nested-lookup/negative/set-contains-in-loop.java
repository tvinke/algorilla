import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SetContainsInLoop {
    public void findMatches(List<String> items, Set<String> targetSet) {
        for (String item : items) {
            if (targetSet.contains(item)) {
                System.out.println(item);
            }
        }
    }
}
