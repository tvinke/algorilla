public class PasswordEncoderAdapter {
    private String extractId(String prefixEncodedPassword) {
        if (prefixEncodedPassword == null) {
            return null;
        }
        int start = prefixEncodedPassword.indexOf("{");
        if (start != 0) {
            return null;
        }
        int end = prefixEncodedPassword.indexOf("}", start);
        if (end < 0) {
            return null;
        }
        return prefixEncodedPassword.substring(start + 1, end);
    }
}
