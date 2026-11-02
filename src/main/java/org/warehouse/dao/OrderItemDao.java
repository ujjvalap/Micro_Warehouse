package org.warehouse.dao;

import org.warehouse.model.OrderItem;

import java.util.List;

/**
 * Data Access Object interface for Order Line Items operations.
 * Specifically handles batch inserts via Spring JdbcTemplate.batchUpdate().
 */
public interface OrderItemDao {

    /**
     * Efficiently inserts multiple line items for an order in a single batch round-trip.
     *
     * @param orderId the parent order ID
     * @param items list of line items to persist
     * @return array containing numbers of rows affected by each update in the batch
     */
    int[] batchInsert(int orderId, List<OrderItem> items);

    /**
     * Retrieves all line items for an order with product name and unit price.
     */
    List<OrderItem> findByOrderId(int orderId);
}
