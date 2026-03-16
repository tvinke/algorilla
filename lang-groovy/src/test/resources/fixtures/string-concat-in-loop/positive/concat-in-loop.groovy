class MessageBuilder {
    String buildMessage(List parts) {
        String result = ""
        for (String part : parts) {
            result = result.concat(part)
        }
        return result
    }
}
