package com.Shravani.dao;

import java.util.List;
import java.util.Map;

public interface SalesDao {

    double getTotalRevenue();

    List<Map<String, Object>> getTopSellingProducts();

    List<Map<String, Object>> getSalesReport();
}
