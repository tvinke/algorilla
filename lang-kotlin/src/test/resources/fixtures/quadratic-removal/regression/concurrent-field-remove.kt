import java.util.concurrent.ConcurrentHashMap

class SessionStore {
    fun cleanup(sessions: ConcurrentHashMap<String, Any>, staleIds: List<String>) {
        for (id in staleIds) {
            sessions.remove(id)
        }
    }
}
