import java.util.Comparator;
import java.util.List;

public class SingleSort {
    public void process(List<String> items) {
        items.sort(Comparator.naturalOrder());
        validate(items);
    }

    private void validate(List<String> items) {}
}
