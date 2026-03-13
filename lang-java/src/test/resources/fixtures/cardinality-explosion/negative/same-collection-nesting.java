class Deduplicator {
    List<Pair> findDuplicates(List<Item> items) {
        List<Pair> dupes = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                if (items.get(i).equals(items.get(j))) {
                    dupes.add(new Pair(items.get(i), items.get(j)));
                }
            }
        }
        return dupes;
    }
}
