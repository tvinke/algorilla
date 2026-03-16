class Processor {
    void process(List<Item> items) {
        for (Item item : items) {
            validate(item);
        }
        for (Item item : items) {
            transform(item);
        }
    }
}
