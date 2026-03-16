class CacheEviction {
    fun evictKeys(expired: List<String>) {
        val cache = mutableMapOf<String, String>()
        cache["a"] = "1"
        cache["b"] = "2"
        for (key in expired) {
            cache.remove(key)
        }
    }
}
