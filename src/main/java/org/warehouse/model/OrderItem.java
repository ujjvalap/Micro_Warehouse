package org.warehouse.model;

import java.math.BigDecimal;

/**
 * Domain entity representing an individual line item within a customer order.
 */
public class OrderItem {
    private int itemId;
    private int orderId;
    private int skuId;
    private int quantity;
    private BigDecimal subtotal;

    // Additional display fields (from join with products table)
    private String productName;
    private BigDecimal unitPrice;

    public OrderItem() {
    }

    public OrderItem(int skuId, int quantity, BigDecimal unitPrice) {
        this.skuId = skuId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        if (unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public OrderItem(int itemId, int orderId, int skuId, int quantity, BigDecimal subtotal) {
        this.itemId = itemId;
        this.orderId = orderId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return String.format("OrderItem[itemId=%d, skuId=%d, product='%s', qty=%d, subtotal=Rs.%.2f]",
                itemId, skuId, productName != null ? productName : "SKU-" + skuId, quantity, subtotal);
    }
}
