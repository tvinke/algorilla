import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;

public class StringTokenizerInLoop {
    public List<String> parseTokens(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input, ",");
        List<String> tokens = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens;
    }

    public List<String> parseWithShortName(String input) {
        StringTokenizer stInternalTokenizer = new StringTokenizer(input, "&");
        List<String> result = new ArrayList<>();
        while (stInternalTokenizer.hasMoreTokens()) {
            result.add(stInternalTokenizer.nextToken());
        }
        return result;
    }
}
