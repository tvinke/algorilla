class SetFilter {
    List findMatches(List items, Set targetSet) {
        List result = new ArrayList();
        items.each(item -> {
            if (targetSet.contains(item)) {
                result.add(item);
            }
        });
        return result;
    }
}
