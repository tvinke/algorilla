import java.util.List;
import java.util.regex.Pattern;

public class OrderService {
    public void validateOrderNumbers(List<Order> orders) {
        for (Order order : orders) {
            Pattern pattern = Pattern.compile("^ORD-\\d{6}$");
            if (!pattern.matcher(order.getOrderNumber()).matches()) {
                throw new IllegalArgumentException("Invalid order number");
            }
        }
    }
}
