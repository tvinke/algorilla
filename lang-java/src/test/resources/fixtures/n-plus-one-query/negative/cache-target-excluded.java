import java.util.List;

public class CacheTargetExcluded {
    private UserCache userCache;
    private MemoStore memoStore;
    private ConnectionPool connectionPool;

    public void lookupAll(List<String> ids) {
        for (String id : ids) {
            Object user = userCache.findById(id);
            Object memo = memoStore.getById(id);
            Object conn = connectionPool.getById(id);
            process(user);
        }
    }

    private void process(Object obj) {}
}

interface UserCache {
    Object findById(String id);
}

interface MemoStore {
    Object getById(String id);
}

interface ConnectionPool {
    Object getById(String id);
}
