
package com.warehouse.dao;

import com.warehouse.model.Product;

import java.util.List;

public interface ProductDao {

    // Add a new product
    int addProduct(Product product);

    // Retrieve all products
    List<Product> getAllProducts();

    // Retrieve a product using SKU ID
    Product getProductById(int skuId);

    // Update product price and category
    int updateProduct(Product product);

    // Increase existing product stock
    int replenishStock(int skuId, int quantity);
}
