import java.util.List;

public class SimpleService {
    public void processAll(List<String> items) {
        for (String item : items) {
            transform(item);
        }
    }

    private String transform(String item) {
        return item.toUpperCase().trim();
    }
}
