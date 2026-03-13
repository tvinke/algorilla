import java.util.List;

public class SingleCharSplit {
    public void process(List<String> lines) {
        for (String line : lines) {
            String[] parts = line.split(",");
            String[] words = line.split(" ");
            String[] segments = line.split("_");
            System.out.println(parts.length + words.length + segments.length);
        }
    }
}
