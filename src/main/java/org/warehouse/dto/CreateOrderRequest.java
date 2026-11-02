package org.warehouse.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO representing a multi-item customer order request.
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

    public void addItem(int skuId, int quantity) {
        this.items.add(new OrderItemRequest(skuId, quantity));
    }

    @Override
    public String toString() {
        return String.format("CreateOrderRequest[phone='%s', itemsCount=%d]", customerPhone, items.size());
    }
}
