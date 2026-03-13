import java.sql.*;
import java.util.*;

public class UserDao {
    public List<User> loadAllUsers(Connection conn) {
        ResultSet rs = conn.prepareStatement("SELECT * FROM users").executeQuery();
        List<User> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapRow(rs));
        }
        return result;
    }
}
