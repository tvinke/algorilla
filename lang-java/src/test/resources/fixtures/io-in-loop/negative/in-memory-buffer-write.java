import java.io.ByteArrayOutputStream;
import java.util.List;

public class InMemoryBufferWrite {
    public byte[] serialize(List<byte[]> chunks) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] chunk : chunks) {
            baos.write(chunk, 0, chunk.length);
        }
        return baos.toByteArray();
    }

    public String buildCsv(List<String> rows) {
        StringBuilder sb = new StringBuilder();
        for (String row : rows) {
            sb.append(row);
        }
        return sb.toString();
    }
}
