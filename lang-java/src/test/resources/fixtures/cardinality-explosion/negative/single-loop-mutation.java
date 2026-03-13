class Collector {
    List<String> collect(List<Item> items) {
        List<String> names = new ArrayList<>();
        for (Item item : items) {
            names.add(item.getName());
        }
        return names;
    }
}
