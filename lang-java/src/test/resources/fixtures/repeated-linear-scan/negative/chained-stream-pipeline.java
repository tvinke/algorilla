class ChainedStreamPipeline {
    void processItems(List<String> items) {
        List<String> result = items.stream().distinct().collect(Collectors.toList());
    }
}
