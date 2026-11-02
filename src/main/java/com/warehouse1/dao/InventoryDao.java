package com.warehouse1.dao;

import com.warehouse1.model.OrderItem;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface InventoryDao {

    Map<Integer, Integer> findStockForUpdate(Collection<Integer> skuIds);

    int[] deductStockBatch(List<OrderItem> items);
}