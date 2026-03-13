class DependsOnLoopVar {
    private Validator validator;

    void process(List<Item> items) {
        for (Item item : items) {
            validator.validate(item);
        }
    }
}
