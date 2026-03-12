class OrderService {
    fun processOrders(orders: List<Order>) {
        for (order in orders) {
            validateItems(order)
        }
    }

    private fun validateItems(order: Order) {
        for (item in order.items) {
            checkStock(item)
        }
    }

    private fun checkStock(item: Item) {
        // point lookup
    }
}
