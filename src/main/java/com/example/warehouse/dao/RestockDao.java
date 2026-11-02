
package com.example.warehouse.dao;

import com.example.warehouse.model.Product;
import java.util.List;

public interface RestockDao {

    // Find products whose stock is below threshold
    List<Product> findLowStockProducts(int threshold);

    // Replenish existing product stock
    int updateStock(int skuId, int quantity);

    // Find product using SKU ID
    Product findProductById(int skuId);
}
