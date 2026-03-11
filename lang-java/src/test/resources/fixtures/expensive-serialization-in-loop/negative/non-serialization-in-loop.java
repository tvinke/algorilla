import java.util.List;
import java.util.ArrayList;

public class ProductDisplayService {
    public List<String> getProductLabels(List<Product> products) {
        List<String> labels = new ArrayList<>();
        for (Product product : products) {
            labels.add(product.getName() + " - " + product.getPrice());
        }
        return labels;
    }
}
