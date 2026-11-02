package com.Shravani.service.impl;

import com.Shravani.dao.SalesDao;
import com.Shravani.service.SalesService;

import java.util.List;
import java.util.Map;

public class SalesServiceImpl implements SalesService {

    private final SalesDao salesDao;

    public SalesServiceImpl(SalesDao salesDao) {
        this.salesDao = salesDao;
    }

    @Override
    public double getTotalRevenue() {
        return salesDao.getTotalRevenue();
    }

    @Override
    public List<Map<String, Object>> getTopSellingProducts() {
        return salesDao.getTopSellingProducts();
    }

    @Override
    public List<Map<String, Object>> getSalesReport() {
        return salesDao.getSalesReport();
    }
}