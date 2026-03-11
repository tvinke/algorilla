import java.util.List;
import java.util.regex.Pattern;

public class WarehouseSearch {
    public void searchByDynamicPattern(List<Product> products, List<String> searchTerms) {
        for (String term : searchTerms) {
            Pattern pattern = Pattern.compile(term);
            for (Product product : products) {
                if (pattern.matcher(product.getName()).find()) {
                    System.out.println("Match: " + product.getName());
                }
            }
        }
    }
}
