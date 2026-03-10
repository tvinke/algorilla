import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexInFilter {
    public List<String> filterByPattern(List<String> items) {
        return items.stream()
            .filter(item -> Pattern.compile("[a-z]+").matcher(item).matches())
            .collect(Collectors.toList());
    }
}
