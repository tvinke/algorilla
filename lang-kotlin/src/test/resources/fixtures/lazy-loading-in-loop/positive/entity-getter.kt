class OrderReport {
    fun generateReport(orderRepository: OrderRepository) {
        val orders = orderRepository.findAll()
        for (order in orders) {
            val items = order.getLineItems()
            println(items.size)
        }
    }
}
