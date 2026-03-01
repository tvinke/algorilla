import java.util.List;

class ItemProcessor {
    void processAll(List items) {
        items.forEach(item -> {
            System.out.println(item);
        });
    }

    void filterItems(List items, List targets) {
        items.forEach(item -> {
            if (targets.contains(item)) {
                System.out.println(item);
            }
        });
    }

    void sortAndGet(List items) {
        items.sort((a, b) -> a.toString().compareTo(b.toString()));
    }
}
