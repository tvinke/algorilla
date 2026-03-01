class OrderService {
    void processOrders(List orders) {
        orders.each(order -> {
            println(order.name);
        });
    }

    List collectNames(List items) {
        return items.collect(it -> it.name);
    }
}
