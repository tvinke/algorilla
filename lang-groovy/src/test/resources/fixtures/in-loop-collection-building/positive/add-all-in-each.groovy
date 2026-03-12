class WarehouseService {
    List getAllProducts(List warehouses) {
        List allProducts = new ArrayList();
        warehouses.each(warehouse -> {
            allProducts.addAll(warehouse.getProducts());
        });
        return allProducts;
    }
}
