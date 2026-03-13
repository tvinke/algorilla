class OrderProcessor {
    List<OrderProduct> combine(List<Order> orders, List<Product> products) {
        List<OrderProduct> results = new ArrayList<>();
        for (Order o : orders) {
            for (Product p : products) {
                results.add(new OrderProduct(o, p));
            }
        }
        return results;
    }
}
