import java.util.*;
import java.util.stream.*;

public class UserRepository {
    public User findById(String targetId) {
        List<User> allUsers = findAll();
        return allUsers.stream()
            .filter(u -> u.getId().equals(targetId))
            .findFirst()
            .orElse(null);
    }

    private List<User> findAll() {
        return new ArrayList<>();
    }
}
