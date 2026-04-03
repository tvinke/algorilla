import java.util.Arrays;
import java.util.List;

public class AssignedStatusChecker {
    public void processOrders(List<Order> orders) {
        List<String> statuses = Arrays.asList("PENDING", "REVIEW", "HOLD");

        for (Order order : orders) {
            if (statuses.contains(order.getStatus())) {
                System.out.println("needs attention: " + order.getId());
            }
        }
    }
}
