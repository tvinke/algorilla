import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class MixedMutationTypes {
    List<String> process(List<Order> orders, List<Item> items) {
        List<String> results = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            for (Item item : items) {
                results.add(item.getName());
                total = total.add(item.getPrice());
            }
        }
        return results;
    }
}
