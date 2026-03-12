public class StringFieldIndexOf {
    private String connectionUrl;

    public String extractScheme() {
        int colon = connectionUrl.indexOf(":");
        int slash = connectionUrl.indexOf("/");
        return connectionUrl.substring(0, Math.min(colon, slash));
    }
}
