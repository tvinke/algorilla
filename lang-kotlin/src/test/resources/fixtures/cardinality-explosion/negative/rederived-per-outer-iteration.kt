class LocaleNameValidator {
    fun validateNames(locales: List<String>) {
        for (locale in locales) {
            val seenNames = HashSet<String>()
            val namesInLocale = getNames(locale)
            for (name in namesInLocale) {
                if (!seenNames.add(name)) {
                    throw IllegalStateException("Duplicate name: $name")
                }
            }
        }
    }

    private fun getNames(locale: String): List<String> = emptyList()
}
