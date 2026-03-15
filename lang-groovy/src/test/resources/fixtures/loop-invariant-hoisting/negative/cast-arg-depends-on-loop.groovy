class TreeNormalizer {
    void normalizeChildren(List children) {
        for (child in children) {
            if (child instanceof Map) {
                normalizeMap((Map) child)
            }
        }
    }
    void normalizeMap(Map m) {
        m.keySet().each { println(it) }
    }
}
