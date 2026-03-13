class Report {
    void summarize(List<Order> orders) {
        double total = orders.stream().mapToDouble(Order::getAmount).sum();
        long count = orders.stream().filter(o -> o.getAmount() > 100).count();
    }
}
