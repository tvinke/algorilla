public class ComparisonService {
    private ProductService productService;

    public boolean isCheaper(String productIdA, String productIdB) {
        double priceA = productService.getProduct(productIdA).getPrice();
        double priceB = productService.getProduct(productIdB).getPrice();
        return priceA < priceB;
    }
}
