class MessageBuilder {
    fun buildMessage(parts: List<String>): String {
        var result = ""
        for (part in parts) {
            result = result.concat(part)
        }
        return result
    }
}
