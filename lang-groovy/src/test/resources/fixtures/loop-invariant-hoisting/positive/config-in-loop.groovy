class BatchProcessor {
    void process(List items) {
        for (Object item : items) {
            int timeout = config.getTimeout();
            item.execute(timeout);
        }
    }
}
