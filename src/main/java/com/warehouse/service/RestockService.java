package com.warehouse.service;

import com.warehouse.model.Product;

import java.util.List;

/**
 * Service interface for automated restock alerts and dark store replenishment (FR-4).
 */
public interface RestockService {

    List<Product> getLowStockProducts(int threshold);

    void replenishStock(int skuId, int quantity);
}
