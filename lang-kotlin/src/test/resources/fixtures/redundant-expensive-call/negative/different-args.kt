class TextNormalizer {
    fun normalize(input: String): String {
        val a = input.replaceAll("\\s+", " ")
        val b = input.replaceAll("\\t+", "\t")
        return "$a $b"
    }
}
