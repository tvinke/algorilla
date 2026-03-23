import java.util.List;

public class EnumProcessor {
    enum Status { ACTIVE, INACTIVE, PENDING }

    // Outer loop iterates enum values (constant-size) — O(k*n) not O(n*m)
    // Should NOT be flagged.
    public void processByStatus(List<Item> items) {
        for (Status status : Status.values()) {
            filterItems(items, status);
        }
    }

    private void filterItems(List<Item> items, Status status) {
        for (Item item : items) {
            if (item.getStatus() == status) {
                item.process();
            }
        }
    }
}
