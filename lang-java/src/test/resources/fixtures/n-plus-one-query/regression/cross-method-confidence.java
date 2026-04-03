import java.util.List;

public class CrossMethodConfidence {
    private UserRepository userRepository;
    private CacheHelper cacheHelper;

    // Cross-method via repo helper → should be HIGH confidence
    void processUsersViaRepoHelper(List<String> ids) {
        for (String id : ids) {
            loadUser(id);
        }
    }

    private User loadUser(String id) {
        return userRepository.findById(id);
    }

    // Cross-method via non-repo helper → should stay MEDIUM
    void processUsersViaCacheHelper(List<String> ids) {
        for (String id : ids) {
            lookupFromCache(id);
        }
    }

    private User lookupFromCache(String id) {
        return cacheHelper.findById(id);
    }

    // Ambiguity: method named findById but target doesn't match repo pattern
    void processWithAmbiguousHelper(List<String> ids) {
        for (String id : ids) {
            findByIdLocally(id);
        }
    }

    private User findByIdLocally(String id) {
        return localMap.findById(id);
    }

    static class User {}
    private Object localMap;
}

interface UserRepository {
    CrossMethodConfidence.User findById(String id);
}

interface CacheHelper {
    CrossMethodConfidence.User findById(String id);
}
