class Branched {
    void process(List<Item> items, boolean flag) {
        if (flag) {
            items.stream().forEach(this::methodA);
        } else {
            items.stream().forEach(this::methodB);
        }
    }
}
