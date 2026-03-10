import java.util.List;
import java.util.stream.Collectors;

public class SimpleCallback {
    public List<String> toUpperCase(List<String> items) {
        return items.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
    }
}
