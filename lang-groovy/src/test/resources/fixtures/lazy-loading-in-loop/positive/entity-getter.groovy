class OrderReportService {
    void printLineItems(Object orderRepository) {
        List orders = orderRepository.getAll();
        for (Object order : orders) {
            List lineItems = order.getLineItems();
            println(lineItems.size());
        }
    }
}
