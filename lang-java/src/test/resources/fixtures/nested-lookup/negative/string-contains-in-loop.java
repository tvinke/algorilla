import java.util.List;

public class StringContainsInLoop {
    public void checkPasswords(List<String> items, String password) {
        for (String item : items) {
            if (password.contains(item)) {
                System.out.println(item);
            }
        }
    }
}
