class OrderProcessor {
    fun summarize(orders: List<Order>) {
        val totals = mutableListOf<Double>()
        for (order in orders) {
            totals.add(order.total)
        }
        val names = mutableListOf<String>()
        for (order in orders) {
            names.add(order.name)
        }
    }
}
