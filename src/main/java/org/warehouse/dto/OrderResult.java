package org.warehouse.dto;

import org.warehouse.model.Order;
import org.warehouse.model.OrderStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO returned after attempting to create and process an order.
 * Captures atomic status, rejection reasons, and fulfillment metrics.
 */
public class OrderResult {
    private boolean successful;
    private OrderStatus status;
    private String message;
    private Order order;
    private List<String> rejectionReasons = new ArrayList<>();
    private long executionTimeMs;

    public OrderResult() {
    }

    public static OrderResult confirmed(Order order, String message, long executionTimeMs) {
        OrderResult result = new OrderResult();
        result.setSuccessful(true);
        result.setStatus(OrderStatus.CONFIRMED);
        result.setOrder(order);
        result.setMessage(message);
        result.setExecutionTimeMs(executionTimeMs);
        return result;
    }

    public static OrderResult rejected(Order order, String message, List<String> reasons, long executionTimeMs) {
        OrderResult result = new OrderResult();
        result.setSuccessful(false);
        result.setStatus(OrderStatus.REJECTED);
        result.setOrder(order);
        result.setMessage(message);
        result.setRejectionReasons(reasons != null ? reasons : new ArrayList<>());
        result.setExecutionTimeMs(executionTimeMs);
        return result;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<String> getRejectionReasons() {
        return rejectionReasons;
    }

    public void setRejectionReasons(List<String> rejectionReasons) {
        this.rejectionReasons = rejectionReasons;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    @Override
    public String toString() {
        return String.format("OrderResult[status=%s, msg='%s', reasons=%s, elapsed=%dms]",
                status, message, rejectionReasons, executionTimeMs);
    }
}
