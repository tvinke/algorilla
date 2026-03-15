import java.util.List;

/**
 * Type-check predicates (is*, has*) called multiple times in the same function.
 * These are O(1) boolean checks — too cheap to flag as redundant.
 * Should NOT trigger redundant-expensive-call.
 */
public class TypeCheckPredicates {

    interface Element {
        String getType();
    }

    static boolean isTextElement(Element el) {
        return "text".equals(el.getType());
    }

    static boolean isArrowElement(Element el) {
        return "arrow".equals(el.getType());
    }

    static boolean hasChildren(Element el) {
        return el.getType() != null;
    }

    public void processElement(Element el) {
        if (isTextElement(el)) {
            System.out.println("text");
        }
        if (isTextElement(el)) {
            System.out.println("also text");
        }
        if (isArrowElement(el)) {
            System.out.println("arrow: " + hasChildren(el));
        }
        if (hasChildren(el)) {
            System.out.println("has children");
        }
    }
}
