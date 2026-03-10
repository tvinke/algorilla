import java.util.List;

public class ValidationService {
    public void validateAll(List<Order> orders) {
        for (Order order : orders) {
            checkOrder(order);
        }
    }

    private void checkOrder(Order order) {
        if (order.getTotal() < 0) {
            throw new IllegalArgumentException("Negative total");
        }
        if (order.getItems() == null) {
            throw new IllegalArgumentException("No items");
        }
    }
}
