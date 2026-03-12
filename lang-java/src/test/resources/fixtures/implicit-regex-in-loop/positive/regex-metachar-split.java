import java.util.List;

public class RegexMetacharSplit {
    public void process(List<String> lines) {
        for (String line : lines) {
            String[] parts = line.split(".");
            System.out.println(parts.length);
        }
    }
}
