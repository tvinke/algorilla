import java.util.*;

class TreeCounter {
    int count(Node node) {
        int total = 1;
        for (Node child : node.getChildren()) {
            total += count(child);
        }
        return total;
    }
}
