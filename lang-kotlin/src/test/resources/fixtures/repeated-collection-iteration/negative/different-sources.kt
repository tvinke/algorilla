class ReportBuilder {
    fun buildReport(orders: List<Order>, customers: List<Customer>) {
        val totals = mutableListOf<Double>()
        for (order in orders) {
            totals.add(order.total)
        }
        val names = mutableListOf<String>()
        for (customer in customers) {
            names.add(customer.name)
        }
    }
}
