import java.util.*;

public class Cleaner {
    public void removeInvalid(List<String> items, Set<String> invalid) {
        for (String bad : invalid) {
            items.remove(bad);
        }
    }
}
