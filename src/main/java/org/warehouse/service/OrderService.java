package org.warehouse.service;

import org.warehouse.dto.CreateOrderRequest;
import org.warehouse.dto.OrderResult;
import org.warehouse.model.Order;
import org.warehouse.model.OrderStatus;
import org.warehouse.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Service interface governing Quick-Commerce order lifecycle management,
 * batch item persistence, and atomic inventory deduction.
 */
public interface OrderService {

    /**
     * FR-2 & FR-3: Atomically verifies inventory, creates an order, deducts stock if sufficient,
     * and performs single-trip batch persistence for all line items via batchUpdate().
     * Rejects the order atomically without deducting stock if any SKU is insufficient.
     */
    OrderResult createAndProcessOrder(CreateOrderRequest request);

    /**
     * Retrieves an order by its unique ID, enriched with all its itemized line items.
     */
    Optional<Order> getOrderById(int orderId);

    /**
     * Retrieves all customer orders in the system, enriched with line items.
     */
    List<Order> getAllOrders();

    /**
     * Retrieves orders filtered by status (PENDING, CONFIRMED, REJECTED, DISPATCHED).
     */
    List<Order> getOrdersByStatus(OrderStatus status);

    /**
     * Retrieves all orders for a specific customer phone number.
     */
    List<Order> getOrdersByCustomerPhone(String customerPhone);

    /**
     * Dispatches a CONFIRMED order for 10-minute quick-commerce delivery.
     */
    boolean dispatchOrder(int orderId);

    /**
     * Retrieves catalog products to facilitate order creation and stock inspection.
     */
    List<Product> getAllProducts();
}
