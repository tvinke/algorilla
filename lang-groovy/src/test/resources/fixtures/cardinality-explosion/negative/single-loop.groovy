class TransformService {
    List transform(List items) {
        return items.collect(item -> {
            item.toUpperCase()
        });
    }
}
