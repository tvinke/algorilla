import java.math.BigDecimal;
import java.util.List;

class ScalarAccumulation {
    BigDecimal totalWeight(List<Order> orders, List<Item> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            for (Item item : items) {
                total = total.add(item.getWeight());
            }
        }
        return total;
    }
}
