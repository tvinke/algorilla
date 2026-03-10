import java.util.List;
import java.util.stream.Collectors;

public class NestedIterationInReduce {
    public int countMatches(List<String> outer, List<String> inner) {
        return outer.stream()
            .reduce(0, (count, item) -> {
                long matches = inner.stream()
                    .filter(i -> i.equals(item))
                    .count();
                return count + (int) matches;
            }, Integer::sum);
    }
}
