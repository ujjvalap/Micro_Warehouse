package com.warehouse.dao;

import com.warehouse.model.OrderItem;

import java.util.List;

/**
 * Data Access Object interface for Order Items table.
 * Supports batch insertion via JdbcTemplate.batchUpdate().
 */
public interface OrderItemDao {

    int[] batchInsert(int orderId, List<OrderItem> items);

    List<OrderItem> findByOrderId(int orderId);
}
