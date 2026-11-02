package com.warehouse1.model;

public class OrderItem {
    private int skuId;
    private int quantity;

    public OrderItem(int skuId, int quantity) {
        this.skuId = skuId;
        this.quantity = quantity;
    }

    public int getSkuId() { return skuId; }
    public int getQuantity() { return quantity; }
}
