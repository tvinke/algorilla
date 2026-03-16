import java.util.List;

public class WildcardFilter {
    public void filterByPattern(List<String> entries, String pattern) {
        for (String entry : entries) {
            if (entry.contains(pattern)) {
                System.out.println("match: " + entry);
            }
        }
    }
}
