import java.util.List;

public class ProductService {
    private List<Product> products;

    public String getProductName(String productId) {
        Product product = getProduct(productId);
        return product.getName();
    }

    private Product getProduct(String productId) {
        for (Product p : products) {
            if (p.getId().equals(productId)) return p;
        }
        return null;
    }
}
