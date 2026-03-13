import java.util.*;

public class OrderService {
    private RestTemplate restTemplate;

    public List<OrderDetails> enrichOrders(List<Order> orders) {
        List<OrderDetails> details = new ArrayList<>();
        for (Order order : orders) {
            OrderDetails d = restTemplate.getForObject("/api/details/" + order.getId(), OrderDetails.class);
            details.add(d);
        }
        return details;
    }
}
