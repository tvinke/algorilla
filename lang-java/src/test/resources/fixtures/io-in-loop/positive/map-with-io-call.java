class MapWithIOCall {
    void processCustomers(List<String> customerIds) {
        List<Customer> customers = customerIds.stream()
            .map(id -> repository.find(id))
            .collect(Collectors.toList());
    }
}
