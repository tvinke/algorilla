import java.util.List;
import java.util.Set;

public class SetFieldContainsInLoop {
    private Set<String> allowedIds;

    public void findMatches(List<String> items) {
        for (String item : items) {
            if (allowedIds.contains(item)) {
                System.out.println(item);
            }
        }
    }
}
