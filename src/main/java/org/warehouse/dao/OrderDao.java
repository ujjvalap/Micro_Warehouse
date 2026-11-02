package org.warehouse.dao;

import org.warehouse.model.Order;
import org.warehouse.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Customer Orders.
 */
public interface OrderDao {

    /**
     * Inserts a new order and returns the persisted entity with generated orderId.
     */
    Order createOrder(Order order);

    /**
     * Updates status for an existing order.
     */
    boolean updateOrderStatus(int orderId, OrderStatus status);

    /**
     * Finds an order header by its unique ID.
     */
    Optional<Order> findById(int orderId);

    /**
     * Retrieves all orders ordered chronologically descending.
     */
    List<Order> findAll();

    /**
     * Retrieves all orders filtered by a specific status.
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Retrieves all orders for a customer phone number.
     */
    List<Order> findByCustomerPhone(String customerPhone);
}
