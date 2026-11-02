package com.warehouse.dao;

import com.warehouse.model.Order;
import com.warehouse.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Orders header table.
 */
public interface OrderDao {

    Order createOrder(Order order);

    Optional<Order> findById(int orderId);

    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerPhone(String customerPhone);

    boolean updateOrderStatus(int orderId, OrderStatus newStatus);
}
