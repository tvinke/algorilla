import java.util.List;
import java.util.ArrayList;

public class OrderAggregator {
    public List<OrderLine> aggregateLines(List<Order> orders) {
        List<OrderLine> lines = new ArrayList<>();
        List<OrderLine> pending = collectPending(orders);
        lines.addAll(pending);
        return lines;
    }

    private List<OrderLine> collectPending(List<Order> orders) {
        return new ArrayList<>();
    }
}
