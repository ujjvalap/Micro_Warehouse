package com.example.warehouse.service;

import com.example.warehouse.model.Product;
import java.util.List;

public interface RestockService {

    // Get all products below minimum stock
    List<Product> getLowStockProducts(int threshold);

    // Replenish stock
    void replenishStock(int skuId, int quantity);

}