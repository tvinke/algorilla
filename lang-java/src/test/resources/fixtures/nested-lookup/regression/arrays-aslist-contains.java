import java.util.Arrays;
import java.util.List;

public class StatusChecker {
    public void processOrders(List<Order> orders) {
        for (Order order : orders) {
            if (Arrays.asList("PENDING", "REVIEW", "HOLD").contains(order.getStatus())) {
                System.out.println("needs attention: " + order.getId());
            }
        }
    }
}
