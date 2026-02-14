import java.util.List;
import java.util.Iterator;

public class ListContainsInWhile {
    public void checkAll(List<String> items, List<String> targets) {
        Iterator<String> iter = items.iterator();
        while (iter.hasNext()) {
            String item = iter.next();
            if (targets.contains(item)) {
                System.out.println(item);
            }
        }
    }
}
