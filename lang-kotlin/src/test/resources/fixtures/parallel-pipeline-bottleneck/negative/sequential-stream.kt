class DataProcessor {
    fun process(items: List<String>): List<String> {
        val results = mutableListOf<String>()
        items.stream().forEach { item ->
            results.add(item.uppercase())
        }
        return results
    }
}
