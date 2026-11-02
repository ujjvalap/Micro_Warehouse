package com.warehouse.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload containing customer contact phone and line items.
 */
public class CreateOrderRequest {

    private String customerPhone;
    private List<OrderItemRequest> items = new ArrayList<>();

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String customerPhone, List<OrderItemRequest> items) {
        this.customerPhone = customerPhone;
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "customerPhone='" + customerPhone + '\'' +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}
