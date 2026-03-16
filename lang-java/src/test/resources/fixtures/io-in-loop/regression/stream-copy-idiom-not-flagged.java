import java.io.InputStream;
import java.io.OutputStream;

public class StreamCopy {
    // Standard stream copy idiom: read from input, write to output via buffer.
    // This is the correct pattern, not an anti-pattern.
    public void copy(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
}
