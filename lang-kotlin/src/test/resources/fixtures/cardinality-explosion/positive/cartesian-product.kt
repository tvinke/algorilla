class ProductBuilder {
    fun combine(colors: List<String>, sizes: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (color in colors) {
            for (size in sizes) {
                result.add("$color-$size")
            }
        }
        return result
    }
}
