class ItemProcessor {
    fun processAll(items: List<String>) {
        items.forEach { item ->
            println(item)
        }
    }

    fun filterItems(items: List<String>, targets: List<String>) {
        items.forEach { item ->
            if (targets.contains(item)) {
                println(item)
            }
        }
    }

    fun sortAndGet(items: MutableList<String>) {
        items.sort()
    }
}
