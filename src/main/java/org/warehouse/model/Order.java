package org.warehouse.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a customer order in the Quick-Commerce fulfillment hub.
 */
public class Order {
    private int orderId;
    private String customerPhone;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(String customerPhone, OrderStatus orderStatus) {
        this.customerPhone = customerPhone;
        this.orderStatus = orderStatus;
        this.createdAt = LocalDateTime.now();
    }

    public Order(int orderId, String customerPhone, OrderStatus orderStatus, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.customerPhone = customerPhone;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    /**
     * Calculates the grand total of the order by summing item subtotals.
     */
    public BigDecimal getTotalAmount() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(OrderItem::getSubtotal)
                .filter(sub -> sub != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return String.format("Order[orderId=%d, phone='%s', status=%s, itemsCount=%d, total=Rs.%.2f, time=%s]",
                orderId, customerPhone, orderStatus, items.size(), getTotalAmount(), createdAt);
    }
}
