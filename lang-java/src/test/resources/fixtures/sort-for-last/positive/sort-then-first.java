import java.util.*;
import java.util.stream.*;

public class OrderService {
    public Product getCheapest(List<Product> products) {
        return products.stream()
            .sorted(Comparator.comparing(Product::getPrice))
            .findFirst()
            .orElse(null);
    }
}
