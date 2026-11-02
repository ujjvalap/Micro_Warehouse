package com.warehouse.dao;

import com.warehouse.model.TopMoverReport;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for Sales Revenue & Top-Mover Audit (FR-5).
 */
public interface SalesDao {

    BigDecimal getTotalFulfillmentRevenue();

    List<TopMoverReport> getTopSellingProducts(int limit);

    List<Map<String, Object>> getCategoryRevenueReport();

    List<Map<String, Object>> getDailyRevenueReport();
}
