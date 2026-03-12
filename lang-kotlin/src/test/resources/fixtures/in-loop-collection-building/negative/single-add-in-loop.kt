class CartService {
    fun collect(items: List<Item>): List<Item> {
        val result = mutableListOf<Item>()
        for (item in items) {
            result.add(item)
        }
        return result
    }
}
