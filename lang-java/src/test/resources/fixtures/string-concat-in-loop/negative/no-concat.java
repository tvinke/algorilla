import java.util.*;

public class SafeBuilder {
    public String buildCsv(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item).append(",");
        }
        return sb.toString();
    }
}
