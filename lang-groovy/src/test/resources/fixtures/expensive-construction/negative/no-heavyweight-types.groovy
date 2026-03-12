class SimpleService {
    String greet(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello, ");
        sb.append(name);
        return sb.toString();
    }
}
