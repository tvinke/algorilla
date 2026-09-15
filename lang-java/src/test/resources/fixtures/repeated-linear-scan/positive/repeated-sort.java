import java.util.Comparator;
import java.util.List;

public class RepeatedSort {
    public void process(List<String> items) {
        items.sort(Comparator.naturalOrder());
        validate(items);
        items.sort(Comparator.naturalOrder());
    }

    private void validate(List<String> items) {}
}
