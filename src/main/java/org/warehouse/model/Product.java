package org.warehouse.model;

import java.math.BigDecimal;

/**
 * Domain entity representing a product SKU in the micro-warehouse inventory.
 */
public class Product {
    private int skuId;
    private String name;
    private String category;
    private BigDecimal unitPrice;
    private int stockQty;

    public Product() {
    }

    public Product(int skuId, String name, String category, BigDecimal unitPrice, int stockQty) {
        this.skuId = skuId;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.stockQty = stockQty;
    }

    public int getSkuId() {
        return skuId;
    }

    public void setSkuId(int skuId) {
        this.skuId = skuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    @Override
    public String toString() {
        return String.format("Product[skuId=%d, name='%s', category='%s', price=Rs.%.2f, stock=%d]",
                skuId, name, category, unitPrice, stockQty);
    }
}
