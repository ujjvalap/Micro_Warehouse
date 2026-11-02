package org.warehouse.dao;

import org.warehouse.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Product SKU inventory operations.
 */
public interface ProductDao {

    Optional<Product> findById(int skuId);

    List<Product> findAll();

    List<Product> findLowStockProducts(int threshold);

    /**
     * Atomically deducts stock for a single SKU if sufficient quantity is available.
     * Returns true if successfully deducted, false if insufficient stock or SKU not found.
     */
    boolean deductStock(int skuId, int quantity);

    /**
     * Replenishes stock for a SKU.
     */
    boolean replenishStock(int skuId, int quantity);
}
