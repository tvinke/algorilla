class Different {
    void process(List<Order> orders, List<Product> products) {
        orders.stream().forEach(this::processOrder);
        products.stream().forEach(this::processProduct);
    }
}
