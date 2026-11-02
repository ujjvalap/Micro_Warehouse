package com.warehouse1.service.impl;

import com.warehouse1.dao.InventoryDao;
import com.warehouse1.model.OrderItem;
import com.warehouse1.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryDao inventoryDao;

    public InventoryServiceImpl(InventoryDao inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deductStock(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InsufficientStockException("Order has no items");
        }

        // same SKU twice in one order -> merge quantities
        Map<Integer, Integer> required = new HashMap<>();
        for (OrderItem item : items) {
            required.merge(item.getSkuId(), item.getQuantity(), Integer::sum);
        }

        // 1) lock rows and verify EVERY sku before deducting anything
        Map<Integer, Integer> available = inventoryDao.findStockForUpdate(required.keySet());
        List<String> shortages = new ArrayList<>();
        required.forEach((sku, qty) -> {
            int have = available.getOrDefault(sku, 0);
            if (have < qty) {
                shortages.add("SKU " + sku + " needs " + qty + ", available " + have);
            }
        });
        if (!shortages.isEmpty()) {
            throw new InsufficientStockException(String.join("; ", shortages));
        }

        // 2) deduct (SQL also has stock_qty >= ? as a safety net)
        int[] results = inventoryDao.deductStockBatch(items);
        for (int r : results) {
            if (r == 0) {
                throw new InsufficientStockException("Stock changed during deduction");
            }
        }
    }
}