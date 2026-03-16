class MessageBuilder {
    String buildMessage(List parts) {
        StringBuilder sb = new StringBuilder()
        for (String part : parts) {
            sb.append(part)
        }
        return sb.toString()
    }
}
