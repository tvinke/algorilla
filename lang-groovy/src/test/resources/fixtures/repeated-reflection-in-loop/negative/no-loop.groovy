class MetadataScanner {
    List scanFields(Class clazz) {
        return clazz.getDeclaredFields().collect { it.name }
    }
}
