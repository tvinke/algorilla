import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderExportService {
    private final ObjectMapper mapper = new ObjectMapper();

    public void exportOrders(List<Order> orders) {
        for (Order order : orders) {
            String json = mapper.writeValueAsString(order);
            sendToQueue(json);
        }
    }

    private void sendToQueue(String json) {
        // publish to message queue
    }
}
