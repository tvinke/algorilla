public class ItemService {
    public Item createItem(String name) {
        Item item = new Item();
        item.setName(name);
        return item;
    }
}
