import java.util.stream.Collectors

class DataProcessor {
    fun process(items: List<String>): List<String> {
        val results = mutableListOf<String>()
        items.parallelStream().forEach { item ->
            results.add(item.uppercase())
        }
        return results
    }
}
