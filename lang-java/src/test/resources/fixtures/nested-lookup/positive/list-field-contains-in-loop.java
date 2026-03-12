import java.util.List;

public class ListFieldContainsInLoop {
    private List<String> targets;

    public void findMatches(List<String> items) {
        for (String item : items) {
            if (targets.contains(item)) {
                System.out.println(item);
            }
        }
    }
}
