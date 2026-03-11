import java.util.List;
import java.util.ArrayList;

public class ProductCollector {
    public List<Product> collectActive(List<Product> products) {
        List<Product> active = new ArrayList<>();
        for (Product product : products) {
            if (product.isActive()) {
                active.add(product);
            }
        }
        return active;
    }
}
