
package com.warehouse.service;

import com.warehouse.model.Product;

import java.util.List;

public interface ProductService {

    // Add a new product
    void addProduct(Product product);

    // View all products
    List<Product> getAllProducts();

    // Search product by SKU ID
    Product getProductById(int skuId);

    // Update product price and category
    void updateProduct(Product product);

    // Replenish existing stock
    void replenishStock(int skuId, int quantity);
}
