package com.warehouse.model;

import java.math.BigDecimal;

/**
 * Audit projection model representing a top-moving SKU in the warehouse.
 */
public class TopMoverReport {

    private int skuId;
    private String productName;
    private String category;
    private int totalUnitsSold;
    private BigDecimal totalRevenue;

    public TopMoverReport() {
    }

    public TopMoverReport(int skuId, String productName, String category, int totalUnitsSold, BigDecimal totalRevenue) {
        this.skuId = skuId;
        this.productName = productName;
        this.category = category;
        this.totalUnitsSold = totalUnitsSold;
        this.totalRevenue = totalRevenue;
    }

    public int getSkuId() {
        return skuId;
    }

    public void setSkuId(int skuId) {
        this.skuId = skuId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalUnitsSold() {
        return totalUnitsSold;
    }

    public void setTotalUnitsSold(int totalUnitsSold) {
        this.totalUnitsSold = totalUnitsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    @Override
    public String toString() {
        return "TopMoverReport{" +
                "skuId=" + skuId +
                ", productName='" + productName + '\'' +
                ", category='" + category + '\'' +
                ", totalUnitsSold=" + totalUnitsSold +
                ", totalRevenue=" + totalRevenue +
                '}';
    }
}
