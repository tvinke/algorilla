import java.util.*;

public class ReportService {
    public List<Product> getSortedProducts(List<Product> products) {
        Collections.sort(products);
        return products;
    }
}
