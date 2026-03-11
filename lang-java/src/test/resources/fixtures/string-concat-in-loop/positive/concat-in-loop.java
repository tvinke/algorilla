import java.util.*;

public class Builder {
    public String buildCsv(List<String> items) {
        String result = "";
        for (String item : items) {
            result = result.concat(item).concat(",");
        }
        return result;
    }
}
