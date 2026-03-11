class Cleaner {
    fun removeStale(active: HashSet<String>) {
        for (id in active.toList()) {
            active.remove(id)
        }
    }
}
