class OrderFilter {
    fun findMatches(items: List<String>, targets: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (item in items) {
            if (targets.contains(item)) {
                result.add(item)
            }
        }
        return result
    }
}
