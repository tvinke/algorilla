class DataProcessor {
    List process(List items) {
        List results = new ArrayList()
        items.stream().forEach(item -> results.add(item.toUpperCase()))
        return results
    }
}
