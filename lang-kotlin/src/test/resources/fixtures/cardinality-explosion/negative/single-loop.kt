class ListBuilder {
    fun buildLabels(items: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (item in items) {
            result.add("label: $item")
        }
        return result
    }
}
