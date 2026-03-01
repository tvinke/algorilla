import java.util.*;
import java.util.stream.*;

public class UserService {
    public void processUsers(List<User> users) {
        User admin = users.stream().filter(u -> u.isAdmin()).findFirst().orElse(null);
        User owner = users.stream().filter(u -> u.isOwner()).findFirst().orElse(null);
    }
}
