import java.util.List;
import java.util.ArrayList;

public class SequentialStreamAdd {
    public void process(List<String> items) {
        List<String> results = new ArrayList<>();
        items.stream().forEach(item -> results.add(item.toUpperCase()));
    }
}
