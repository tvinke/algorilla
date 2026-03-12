class ChainExamples {
    fun chainedCalls(items: List<String>) {
        items.map { it.uppercase() }.filter { it.isNotEmpty() }.forEach { println(it) }
    }

    fun objectCreation() {
        val map = HashMap<String, Int>()
        val set = HashSet<String>()
    }

    fun propertyDeclaration() {
        val name: String = "hello"
        val count = items.size
    }
}
