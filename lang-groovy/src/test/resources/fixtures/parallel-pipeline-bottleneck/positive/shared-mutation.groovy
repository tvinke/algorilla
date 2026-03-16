class DataProcessor {
    List process(List items) {
        List results = new ArrayList()
        items.parallelStream().forEach(item -> results.add(item.toUpperCase()))
        return results
    }
}
