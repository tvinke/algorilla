import java.util.List;

public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public void processOrders(List<Order> orders) {
        for (Order order : orders) {
            repository.persist(order);  // calls method on repository, not local persist()
        }
    }

    // Local method named persist — should NOT be resolved for repository.persist()
    private long persist(Document document) {
        for (Item item : document.getItems()) {
            System.out.println(item);
        }
        return document.getItems().size();
    }
}
