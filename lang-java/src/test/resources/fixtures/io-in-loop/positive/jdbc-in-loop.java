import java.sql.*;
import java.util.*;

public class UserDao {
    public List<User> loadUsers(Connection conn, List<String> ids) {
        List<User> result = new ArrayList<>();
        for (String id : ids) {
            ResultSet rs = conn.prepareStatement("SELECT * FROM users WHERE id = ?").executeQuery();
            result.add(mapRow(rs));
        }
        return result;
    }
}
