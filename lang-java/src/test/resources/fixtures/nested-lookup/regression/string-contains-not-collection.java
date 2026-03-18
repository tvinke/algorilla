import java.util.List;

public class StringContainsNotCollection {
    // String.contains(substring) is O(n) on string length, not a collection lookup.
    // Should NOT be flagged as nested-lookup.

    boolean hasMatchingName(List<String> items, String searchText) {
        for (String item : items) {
            if (searchText.contains(item)) {
                return true;
            }
        }
        return false;
    }

    boolean checkMessages(List<String> keywords, String message) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // Variable name ending in common string suffixes
    boolean scanDescription(List<String> terms, String description) {
        for (String term : terms) {
            if (description.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
