class OrderService {
    void analyzeOrders(List orders) {
        def byStatus = orders.groupBy()
        def uniqueOrders = orders.unique()
    }
}
