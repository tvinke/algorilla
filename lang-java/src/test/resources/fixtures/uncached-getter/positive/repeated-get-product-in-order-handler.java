import java.util.List;

public class OrderHandler {
    private ProductService productService;

    public void handleOrder(String productId, int quantity) {
        String name = productService.getProduct(productId).getName();
        double price = productService.getProduct(productId).getPrice();
        boolean available = productService.getProduct(productId).isAvailable();
        if (available) {
            createOrderLine(name, price, quantity);
        }
    }

    private void createOrderLine(String name, double price, int qty) {}
}
