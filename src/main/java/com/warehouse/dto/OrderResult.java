package com.warehouse.dto;

import com.warehouse.model.Order;
import com.warehouse.model.OrderStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Result object returned after an atomic order placement attempt.
 */
public class OrderResult {

    private final Order order;
    private final OrderStatus status;
    private final String message;
    private final List<String> rejectionReasons;
    private final long executionTimeMs;

    public OrderResult(Order order, OrderStatus status, String message, List<String> rejectionReasons, long executionTimeMs) {
        this.order = order;
        this.status = status;
        this.message = message;
        this.rejectionReasons = rejectionReasons != null ? rejectionReasons : new ArrayList<>();
        this.executionTimeMs = executionTimeMs;
    }

    public static OrderResult confirmed(Order order, String message, long executionTimeMs) {
        return new OrderResult(order, OrderStatus.CONFIRMED, message, new ArrayList<>(), executionTimeMs);
    }

    public static OrderResult rejected(Order order, String message, List<String> rejectionReasons, long executionTimeMs) {
        return new OrderResult(order, OrderStatus.REJECTED, message, rejectionReasons, executionTimeMs);
    }

    public boolean isSuccessful() {
        return status == OrderStatus.CONFIRMED;
    }

    public Order getOrder() {
        return order;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getRejectionReasons() {
        return rejectionReasons;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    @Override
    public String toString() {
        return "OrderResult{" +
                "orderId=" + (order != null ? order.getOrderId() : "null") +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", rejectionReasons=" + rejectionReasons +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}
