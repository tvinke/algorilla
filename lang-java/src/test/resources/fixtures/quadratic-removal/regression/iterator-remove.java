import java.util.*;

public class IteratorRemoveExample {
    public void removeNulls(List<String> items) {
        Iterator<String> it = items.iterator();
        while (it.hasNext()) {
            String item = it.next();
            if (item == null) {
                it.remove();
            }
        }
    }
}
