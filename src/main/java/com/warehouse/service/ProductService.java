package com.warehouse.service;

import com.warehouse.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for SKU Catalog & Inventory Replenishment (FR-1).
 */
public interface ProductService {

    Product addProduct(Product product);

    List<Product> getAllProducts();

    Optional<Product> getProductById(int skuId);

    Product updateProduct(Product product);

    void replenishStock(int skuId, int quantity);
}
