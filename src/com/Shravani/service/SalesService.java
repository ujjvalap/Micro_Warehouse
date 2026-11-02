package com.Shravani.service;

import java.util.List;
import java.util.Map;

public interface SalesService {

    double getTotalRevenue();

    List<Map<String, Object>> getTopSellingProducts();

    List<Map<String, Object>> getSalesReport();
}
