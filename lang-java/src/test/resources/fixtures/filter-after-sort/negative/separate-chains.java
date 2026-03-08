import java.util.*;
import java.util.stream.*;

public class SeparateChains {
    public void process(List<String> items) {
        List<String> sorted = items.stream().sorted().collect(Collectors.toList());
        List<String> filtered = items.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
