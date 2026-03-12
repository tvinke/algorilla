class SimpleService {
    fun greet(name: String): String {
        val sb = StringBuilder()
        sb.append("Hello, ")
        sb.append(name)
        return sb.toString()
    }
}
