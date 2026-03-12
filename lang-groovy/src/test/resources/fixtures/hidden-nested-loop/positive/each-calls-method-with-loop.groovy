class OrderService {
    void processOrders(List orders) {
        orders.each(order -> {
            validateItems(order);
        });
    }

    private void validateItems(Object order) {
        for (Object item : order.items) {
            checkStock(item);
        }
    }

    private void checkStock(Object item) {
        // point lookup
    }
}
