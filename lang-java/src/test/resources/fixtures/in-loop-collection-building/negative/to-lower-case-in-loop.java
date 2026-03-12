import java.util.List;

public class PasswordValidator {
    public boolean validatePasswords(List<String> passwords) {
        for (String password : passwords) {
            String lower = password.toLowerCase();
            if (lower.contains("admin")) {
                return false;
            }
        }
        return true;
    }
}
