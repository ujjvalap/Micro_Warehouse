package com.warehouse.dto;

/**
 * Request payload for an individual item in a customer order.
 */
public class OrderItemRequest {

    private int skuId;
    private int quantity;

    public OrderItemRequest() {
    }

    public OrderItemRequest(int skuId, int quantity) {
        this.skuId = skuId;
        this.quantity = quantity;
    }

    public int getSkuId() {
        return skuId;
    }

    public void setSkuId(int skuId) {
        this.skuId = skuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "OrderItemRequest{skuId=" + skuId + ", quantity=" + quantity + "}";
    }
}
