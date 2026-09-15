import java.util.List;

public class NameHeuristicListNotConfirmed {

    /**
     * 'targets' has no declared type and its source, fetchPriorityList(), is never defined
     * anywhere in this file (or any other), so the only way TypeEnvironment can infer its type
     * at all is the method-name-suffix heuristic ("List" suffix -> NAME_HEURISTIC, low trust).
     * That must NOT be enough on its own to promote this finding to HIGH confidence.
     */
    public void findMatches(List<String> items) {
        var targets = fetchPriorityList();
        for (String item : items) {
            if (targets.contains(item)) {
                System.out.println(item);
            }
        }
    }
}
