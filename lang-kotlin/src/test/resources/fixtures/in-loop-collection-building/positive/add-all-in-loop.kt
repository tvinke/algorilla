class WarehouseService {
    fun getAllProducts(warehouses: List<Warehouse>): List<Product> {
        val allProducts = mutableListOf<Product>()
        for (warehouse in warehouses) {
            allProducts.addAll(warehouse.getProducts())
        }
        return allProducts
    }
}
