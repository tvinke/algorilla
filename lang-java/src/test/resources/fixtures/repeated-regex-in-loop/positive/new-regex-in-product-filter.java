import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ProductCatalog {
    public List<Product> filterBySkuPattern(List<Product> products) {
        List<Product> matched = new ArrayList<>();
        for (Product product : products) {
            Pattern skuPattern = Pattern.compile("SKU-[A-Z]{3}-\\d+");
            if (skuPattern.matcher(product.getSku()).matches()) {
                matched.add(product);
            }
        }
        return matched;
    }
}
