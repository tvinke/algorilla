import java.util.List;

public class ForEachIndexOf {
    public void findPositions(List<String> items, List<String> targets) {
        items.forEach(item -> {
            int pos = targets.indexOf(item);
            if (pos >= 0) {
                System.out.println("Found at " + pos);
            }
        });
    }
}
