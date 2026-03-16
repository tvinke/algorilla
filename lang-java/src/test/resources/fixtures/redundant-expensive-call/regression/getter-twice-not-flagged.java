import java.util.Map;

public class GetterTwiceNotFlagged {
    // Simple getters called twice should not be flagged — they are cheap accessors.
    // Only flag getters at 3+ calls.

    void processAttribute(Map<String, Object> attributes) {
        String name = getAttributeAsString(attributes, "name");
        String label = getAttributeAsString(attributes, "label");
        // Two calls with different args — not the same signature
    }

    void checkBothWithSameArgs(Map<String, Object> attributes) {
        String first = getFirstAttribute(attributes);
        String second = getFirstAttribute(attributes);
        // Same args, but only 2 calls — getter pattern should allow it
    }

    void flagThreeGetter(Map<String, Object> attributes) {
        String a = getFirstAttribute(attributes);
        String b = getFirstAttribute(attributes);
        String c = getFirstAttribute(attributes);
        // 3 calls with same args — should still be flagged
    }

    private String getAttributeAsString(Map<String, Object> attrs, String key) {
        Object val = attrs.get(key);
        return val != null ? val.toString() : "";
    }

    private String getFirstAttribute(Map<String, Object> attrs) {
        return attrs.values().iterator().next().toString();
    }
}
