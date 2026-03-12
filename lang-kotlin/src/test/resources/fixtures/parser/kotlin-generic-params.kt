class GenericParamProcessor {
    fun filterByIds(ids: List<String>, active: HashSet<String>) {
        ids.forEach { id ->
            if (active.contains(id)) {
                println(id)
            }
        }
    }
}
