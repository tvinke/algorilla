import java.util.List;

public class ListContainsInFor {
    public void findMatches(List<String> items, List<String> targets) {
        for (String item : items) {
            if (targets.contains(item)) {
                System.out.println(item);
            }
        }
    }
}
