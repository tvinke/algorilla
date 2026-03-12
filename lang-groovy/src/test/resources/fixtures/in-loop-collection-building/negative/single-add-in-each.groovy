class CartService {
    List collect(List items) {
        List result = new ArrayList();
        items.each(item -> {
            result.add(item);
        });
        return result;
    }
}
