import java.util.List;
import java.util.ArrayList;

class OrderService {
    private List<Order> getAllOrders() {
        return new ArrayList<>();
    }

    void processOrders() {
        var orders = getAllOrders();
        for (String id : ids) {
            // orders is a List (from return type) — this should be a TP with HIGH confidence
            if (orders.contains(id)) {
                process(id);
            }
        }
    }
}
