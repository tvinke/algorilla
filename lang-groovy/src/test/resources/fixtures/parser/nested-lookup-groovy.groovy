class ItemService {
    void filterItems(List items, List targets) {
        items.each(item -> {
            if (targets.contains(item.id)) {
                process(item);
            }
        });
    }
}
