class ConfigInLoop {
    private Config config;

    void process(List<Item> items) {
        for (Item item : items) {
            String setting = config.getTimeout();
            item.apply(setting);
        }
    }
}
