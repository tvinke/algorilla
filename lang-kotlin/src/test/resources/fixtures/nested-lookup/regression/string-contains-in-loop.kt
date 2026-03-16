class KeywordSearch {
    fun filterByKeyword(items: List<String>, keyword: String): List<String> {
        val matches = mutableListOf<String>()
        for (i in items.indices) {
            val item: String = items[i]
            if (item.contains(keyword)) {
                matches.add(item)
            }
        }
        return matches
    }
}
