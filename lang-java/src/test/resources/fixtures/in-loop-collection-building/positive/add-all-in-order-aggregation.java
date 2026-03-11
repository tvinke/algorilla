import java.util.List;
import java.util.ArrayList;

public class WarehouseService {
    public List<Product> getAllProducts(List<Warehouse> warehouses) {
        List<Product> allProducts = new ArrayList<>();
        for (Warehouse warehouse : warehouses) {
            allProducts.addAll(warehouse.getProducts());
        }
        return allProducts;
    }
}
