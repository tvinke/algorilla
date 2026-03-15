class MultiMutationCartesian {
    void process(List<String> items, List<String> categories) {
        List<String> result = new ArrayList<>();
        Map<String, String> index = new HashMap<>();
        for (String item : items) {
            for (String category : categories) {
                result.add(item + category);
                result.add(category + item);
                index.put(item, category);
            }
        }
    }
}
