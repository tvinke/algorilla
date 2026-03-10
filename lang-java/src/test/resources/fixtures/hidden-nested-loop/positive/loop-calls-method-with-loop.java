import java.util.List;

public class OrderService {
    public void processOrders(List<Order> orders) {
        for (Order order : orders) {
            validateItems(order);
        }
    }

    private void validateItems(Order order) {
        for (Item item : order.getItems()) {
            checkInventory(item);
        }
    }

    private void checkInventory(Item item) {
        // point lookup, no loop
    }
}
