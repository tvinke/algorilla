import java.sql.*;
import java.util.*;

public class UnambiguousIOHighConfidence {

    /**
     * executeQuery is an unambiguous io-method — should be HIGH confidence.
     */
    public List<Object> loadAll(Connection conn, List<String> ids) {
        List<Object> result = new ArrayList<>();
        for (String id : ids) {
            ResultSet rs = conn.executeQuery("SELECT * FROM t WHERE id = " + id);
            result.add(rs);
        }
        return result;
    }

    /**
     * save() on a repository target is an io-method-candidate — should be MEDIUM confidence.
     */
    public void saveAll(List<Object> items) {
        for (Object item : items) {
            repository.save(item);
        }
    }

    private Object repository;
}
