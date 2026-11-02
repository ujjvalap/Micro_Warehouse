package com.warehouse.service;

import com.warehouse.dto.CreateOrderRequest;
import com.warehouse.dto.OrderResult;
import com.warehouse.model.Order;
import com.warehouse.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for multi-item batch order creation and atomic stock fulfillment (FR-2 & FR-3).
 */
public interface OrderService {

    OrderResult createAndProcessOrder(CreateOrderRequest request);

    Optional<Order> getOrderById(int orderId);

    List<Order> getAllOrders();

    List<Order> getOrdersByStatus(OrderStatus status);

    List<Order> getOrdersByCustomerPhone(String customerPhone);

    boolean dispatchOrder(int orderId);
}
