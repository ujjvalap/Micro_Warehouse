package com.warehouse1.service;

import com.warehouse1.model.OrderItem;
import java.util.List;

public interface InventoryService {

    void deductStock(List<OrderItem> items);

    class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}