import java.util.List;

public class Pipeline {
    private List<Mapper> mappers;

    // The inner loop iterates a small constant collection of mappers.
    // O(n*k) with constant k — should NOT be flagged.
    public void processAll(List<Item> items) {
        for (Item item : items) {
            applyMappers(item);
        }
    }

    private void applyMappers(Item item) {
        for (Mapper mapper : mappers) {
            mapper.apply(item);
        }
    }
}
