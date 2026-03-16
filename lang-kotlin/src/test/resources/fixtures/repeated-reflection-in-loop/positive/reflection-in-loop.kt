class MetadataScanner {
    fun scanFields(classes: List<Class<*>>): List<String> {
        val result = mutableListOf<String>()
        for (clazz in classes) {
            val fields = clazz.getDeclaredFields()
            for (field in fields) {
                result.add(field.name)
            }
        }
        return result
    }
}
