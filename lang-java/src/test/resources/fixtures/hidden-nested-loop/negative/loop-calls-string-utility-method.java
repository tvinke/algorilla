import java.util.List;
import java.util.ArrayList;

public class StringFilterService {
    public List<String> filterNonEmpty(List<String> items) {
        List<String> result = new ArrayList<>();
        for (String item : items) {
            if (hasText(item)) {
                result.add(trimWhitespace(item));
            }
        }
        return result;
    }

    private boolean hasText(String str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private String trimWhitespace(String str) {
        char[] chars = str.toCharArray();
        int start = 0;
        int end = chars.length;
        while (start < end && Character.isWhitespace(chars[start])) {
            start++;
        }
        while (end > start && Character.isWhitespace(chars[end - 1])) {
            end--;
        }
        return new String(chars, start, end - start);
    }
}
