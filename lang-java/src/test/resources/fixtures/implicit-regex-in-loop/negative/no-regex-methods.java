import java.util.*;

public class Processor {
    public void process(List<String> items) {
        for (String item : items) {
            System.out.println(item.toLowerCase());
            item.trim();
            item.substring(0, 5);
        }
    }
}
