class StringContainsService {
    void filterByKeyword(List<String> items, String keyword) {
        for (item in items) {
            if (item.contains(keyword)) {
                println(item)
            }
        }
    }
}
