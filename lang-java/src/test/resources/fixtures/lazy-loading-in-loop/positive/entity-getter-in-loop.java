class OrderProcessor {
    private OrderRepository orderRepository;

    void processOrders() {
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            List<LineItem> items = order.getLineItems();
            process(items);
        }
    }

    void process(List<LineItem> items) {}
}
