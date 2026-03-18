import java.util.List;

public class RepoTargetHighConfidence {

    private UserRepository userRepository;
    private Object dataService;

    /**
     * Target 'userRepository' matches io-target-patterns — should be HIGH confidence.
     */
    public void loadUsers(List<String> emails) {
        for (String email : emails) {
            Object user = userRepository.findByEmail(email);
            process(user);
        }
    }

    /**
     * Target 'dataService' does not match typical repo/dao patterns exactly
     * (depends on YAML config), but 'service' IS in io-target-patterns.
     * Should be HIGH confidence if matched, MEDIUM otherwise.
     */
    public void loadFromService(List<String> ids) {
        for (String id : ids) {
            Object data = dataService.findById(id);
            process(data);
        }
    }

    private void process(Object o) {}
}

interface UserRepository {
    Object findByEmail(String email);
}
