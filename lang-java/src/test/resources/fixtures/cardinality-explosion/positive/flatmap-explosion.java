class Combiner {
    List<Pair> combine(List<Order> orders, List<Product> products) {
        return orders.stream()
            .flatMap(o -> products.stream().map(p -> new Pair(o, p)))
            .collect(Collectors.toList());
    }
}
