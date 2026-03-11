import java.util.Map;

public class InventoryLookup {
    public void checkStock(Map<String, Integer> inventory, String sku) {
        Integer current = inventory.get(sku);
        Integer reserved = inventory.get(sku);
        if (current != null && reserved != null) {
            System.out.println("Stock: " + current);
        }
    }
}
