public class KeyUtils {
    public String cleanPem(String pem) {
        pem = pem.replaceAll("-----BEGIN(.*?)KEY-----", "");
        pem = pem.replaceAll("-----END(.*?)KEY-----", "");
        pem = pem.replaceAll("\r\n", "");
        pem = pem.replaceAll("\n", "");
        pem = pem.replaceAll("\\\\n", "");
        return pem;
    }
}
