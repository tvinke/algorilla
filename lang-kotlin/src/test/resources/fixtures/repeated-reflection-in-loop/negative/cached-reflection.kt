class MetadataScanner {
    fun scanFields(classes: List<Class<*>>): Map<String, List<String>> {
        return classes.associate { clazz ->
            clazz.name to clazz.declaredFields.map { it.name }
        }
    }
}
