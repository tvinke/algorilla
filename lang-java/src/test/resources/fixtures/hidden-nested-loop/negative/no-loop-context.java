import java.util.List;

public class BatchProcessor {
    public void process(List<Order> orders) {
        // method with loop called once, not from inside a loop
        validateAll(orders);
    }

    private void validateAll(List<Order> orders) {
        for (Order order : orders) {
            check(order);
        }
    }

    private void check(Order order) {
        // no loop
    }
}
