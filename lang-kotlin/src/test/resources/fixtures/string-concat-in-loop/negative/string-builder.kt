class MessageBuilder {
    fun buildMessage(parts: List<String>): String {
        val sb = StringBuilder()
        for (part in parts) {
            sb.append(part)
        }
        return sb.toString()
    }
}
