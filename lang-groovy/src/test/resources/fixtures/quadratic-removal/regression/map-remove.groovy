class MapRemoveService {
    void cleanUp(Map<String, String> cache, List<String> expired) {
        for (key in expired) {
            cache.remove(key)
        }
    }
}
