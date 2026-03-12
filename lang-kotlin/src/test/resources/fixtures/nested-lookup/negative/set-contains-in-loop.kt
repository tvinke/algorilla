class SetFilter {
    fun findMatches(items: List<String>, targetSet: Set<String>): List<String> {
        val result = mutableListOf<String>()
        for (item in items) {
            if (targetSet.contains(item)) {
                result.add(item)
            }
        }
        return result
    }
}
