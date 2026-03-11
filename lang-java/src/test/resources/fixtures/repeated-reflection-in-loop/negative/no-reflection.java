import java.util.*;

public class SimpleProcessor {
    public void process(List<String> items) {
        for (String item : items) {
            System.out.println(item.toUpperCase());
        }
    }
}
