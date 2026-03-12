class OrderFilter {
    List findMatches(List items, List targets) {
        List result = new ArrayList();
        items.each(item -> {
            if (targets.contains(item)) {
                result.add(item);
            }
        });
        return result;
    }
}
