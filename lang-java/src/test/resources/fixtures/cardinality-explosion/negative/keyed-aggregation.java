import java.util.HashMap;
import java.util.List;
import java.util.Map;

class KeyedAggregation {
    Map<String, Double> lastPrice(List<Order> orders, List<Product> products) {
        Map<String, Double> priceMap = new HashMap<>();
        for (Order order : orders) {
            for (Product product : products) {
                priceMap.put(product.getSku(), product.getPrice());
            }
        }
        return priceMap;
    }
}
