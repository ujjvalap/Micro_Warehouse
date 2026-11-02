package com.warehouse.dao;

import com.warehouse.model.OrderItem;
import com.warehouse.model.Product;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object interface for Product Catalog & Inventory operations.
 */
public interface ProductDao {

    int addProduct(Product product);

    List<Product> findAll();

    Optional<Product> findById(int skuId);

    int updateProduct(Product product);

    int replenishStock(int skuId, int quantity);

    boolean deductStock(int skuId, int quantity);

    Map<Integer, Integer> findStockForUpdate(Collection<Integer> skuIds);

    int[] deductStockBatch(List<OrderItem> items);

    List<Product> findLowStockProducts(int threshold);
}
