package com.warehouse.service.impl;

import com.warehouse.dao.SalesDao;
import com.warehouse.model.TopMoverReport;
import com.warehouse.service.SalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for revenue calculations and top-mover audits (FR-5).
 */
@Service
public class SalesServiceImpl implements SalesService {

    private static final Logger logger = LoggerFactory.getLogger(SalesServiceImpl.class);

    private final SalesDao salesDao;

    public SalesServiceImpl(SalesDao salesDao) {
        this.salesDao = salesDao;
    }

    @Override
    public BigDecimal getTotalRevenue() {
        try {
            BigDecimal total = salesDao.getTotalFulfillmentRevenue();
            logger.info("Total warehouse fulfillment revenue: Rs.{}", total);
            return total;
        } catch (DataAccessException e) {
            logger.error("Error retrieving total revenue", e);
            throw new RuntimeException("Failed to calculate total revenue", e);
        }
    }

    @Override
    public List<TopMoverReport> getTopSellingProducts(int limit) {
        if (limit <= 0) {
            limit = 5;
        }
        try {
            return salesDao.getTopSellingProducts(limit);
        } catch (DataAccessException e) {
            logger.error("Error retrieving top selling products", e);
            throw new RuntimeException("Failed to retrieve top-moving SKUs", e);
        }
    }

    @Override
    public List<Map<String, Object>> getCategoryRevenueReport() {
        try {
            return salesDao.getCategoryRevenueReport();
        } catch (DataAccessException e) {
            logger.error("Error generating category revenue report", e);
            throw new RuntimeException("Failed to generate category report", e);
        }
    }

    @Override
    public List<Map<String, Object>> getDailyRevenueReport() {
        try {
            return salesDao.getDailyRevenueReport();
        } catch (DataAccessException e) {
            logger.error("Error generating daily revenue report", e);
            throw new RuntimeException("Failed to generate daily sales report", e);
        }
    }
}
