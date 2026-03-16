import java.io.InputStream;
import java.util.List;

public class CloseIsCleanup {
    // close() in a loop is resource cleanup, not data-transfer IO.
    // Should NOT be flagged as io-in-loop.

    void closeStreamsInLoop(List<InputStream> streams) {
        for (InputStream stream : streams) {
            stream.close();
        }
    }

    void closeConnectionsInLoop(List<AutoCloseable> resources) {
        for (AutoCloseable resource : resources) {
            try {
                resource.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
