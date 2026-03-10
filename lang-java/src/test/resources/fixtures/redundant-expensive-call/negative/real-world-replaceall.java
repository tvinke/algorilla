import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class KeyUtils {

    public static java.security.PrivateKey loadPrivateKey(String pemContent) throws Exception {
        String pem = pemContent;
        pem = pem.replaceAll("-----BEGIN(.*?)KEY-----", "");
        pem = pem.replaceAll("-----END(.*?)KEY-----", "");
        pem = pem.replaceAll("\r\n", "");
        pem = pem.replaceAll("\n", "");
        pem = pem.replaceAll("\\\\n", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    public static boolean isSupported(java.security.spec.AlgorithmParameterSpec algo) {
        if (!algo.toString().startsWith("RS") && !algo.toString().startsWith("PS")) {
            if (algo.toString().startsWith("ES")) {
                return false;
            }
        }
        return true;
    }
}
