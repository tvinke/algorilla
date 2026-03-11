import java.util.List;

public class OrderService {
    private OrderRepository orderRepository;
    private ProductRepository productRepository;

    public void processOrders(List<String> emails) {
        for (String email : emails) {
            Object order = orderRepository.findByEmail(email);
            Object tracking = orderRepository.getByTrackingNumber(email);
            Object sku = productRepository.getBySku(email);
            process(order);
        }
    }

    private void process(Object order) {}
}

interface OrderRepository {
    Object findByEmail(String email);
    Object getByTrackingNumber(String number);
}

interface ProductRepository {
    Object getBySku(String sku);
}
