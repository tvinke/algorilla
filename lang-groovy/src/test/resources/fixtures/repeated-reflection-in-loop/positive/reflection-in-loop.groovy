class MetadataScanner {
    List scanFields(List classes) {
        List result = new ArrayList()
        for (Class clazz : classes) {
            def fields = clazz.getDeclaredFields()
            for (def field : fields) {
                result.add(field.name)
            }
        }
        return result
    }
}
