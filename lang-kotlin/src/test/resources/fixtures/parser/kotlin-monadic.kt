class MonadicService {
    fun processResult(result: Result<String>) {
        result.map { it.uppercase() }
        result.flatMap { Result.success(it) }
    }
    fun processCollection(items: List<String>) {
        items.map { it.uppercase() }
        items.forEach { println(it) }
    }
}
