class IteratorRemoveService {
    void removeInvalid(List<String> items) {
        def iter = items.iterator()
        while (iter.hasNext()) {
            def item = iter.next()
            if (item == null) {
                iter.remove()
            }
        }
    }
}
