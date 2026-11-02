package com.warehouse.service;

import com.warehouse.model.TopMoverReport;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Sales Revenue & Top-Mover Audit (FR-5).
 */
public interface SalesService {

    BigDecimal getTotalRevenue();

    List<TopMoverReport> getTopSellingProducts(int limit);

    List<Map<String, Object>> getCategoryRevenueReport();

    List<Map<String, Object>> getDailyRevenueReport();
}
