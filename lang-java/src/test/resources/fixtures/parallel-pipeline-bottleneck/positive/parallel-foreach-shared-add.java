import java.util.List;
import java.util.ArrayList;

public class ParallelBottleneckPositive {
    public void process(List<String> items) {
        List<String> results = new ArrayList<>();
        items.parallelStream().forEach(item -> results.add(item.toUpperCase()));
    }
}
