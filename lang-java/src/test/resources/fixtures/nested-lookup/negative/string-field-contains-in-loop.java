import java.util.List;

public class StringFieldContainsInLoop {
    private String metaServerAddress;

    public void process(List<String> items) {
        for (String item : items) {
            if (metaServerAddress.contains(item)) {
                System.out.println(item);
            }
        }
    }
}
