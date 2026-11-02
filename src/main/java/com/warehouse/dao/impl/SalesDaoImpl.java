package com.warehouse.dao.impl;

import com.warehouse.dao.SalesDao;
import com.warehouse.model.TopMoverReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Spring JdbcTemplate implementation for sales audits and fulfillment revenue metrics.
 */
@Repository
public class SalesDaoImpl implements SalesDao {

    private static final Logger logger = LoggerFactory.getLogger(SalesDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public SalesDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TopMoverReport> topMoverRowMapper = (rs, rowNum) -> new TopMoverReport(
            rs.getInt("sku_id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getInt("total_sold"),
            rs.getBigDecimal("total_revenue")
    );

    @Override
    public BigDecimal getTotalFulfillmentRevenue() {
        String sql = """
                SELECT COALESCE(SUM(oi.subtotal), 0.00) AS total_revenue
                FROM order_items oi
                JOIN orders o ON oi.order_id = o.order_id
                WHERE o.order_status IN ('CONFIRMED', 'DISPATCHED')
                """;
        try {
            BigDecimal revenue = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return revenue != null ? revenue : BigDecimal.ZERO;
        } catch (DataAccessException e) {
            logger.error("Failed to calculate total fulfillment revenue", e);
            throw e;
        }
    }

    @Override
    public List<TopMoverReport> getTopSellingProducts(int limit) {
        String sql = """
                SELECT p.sku_id, p.name, p.category,
                       SUM(oi.quantity) AS total_sold,
                       SUM(oi.subtotal) AS total_revenue
                FROM order_items oi
                JOIN products p ON oi.sku_id = p.sku_id
                JOIN orders o ON oi.order_id = o.order_id
                WHERE o.order_status IN ('CONFIRMED', 'DISPATCHED')
                GROUP BY p.sku_id, p.name, p.category
                ORDER BY total_sold DESC, total_revenue DESC
                LIMIT ?
                """;
        try {
            return jdbcTemplate.query(sql, topMoverRowMapper, limit);
        } catch (DataAccessException e) {
            logger.error("Failed to query top selling products", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getCategoryRevenueReport() {
        String sql = """
                SELECT p.category,
                       COUNT(DISTINCT oi.order_id) AS total_orders,
                       SUM(oi.quantity) AS total_units_sold,
                       SUM(oi.subtotal) AS category_revenue
                FROM order_items oi
                JOIN products p ON oi.sku_id = p.sku_id
                JOIN orders o ON oi.order_id = o.order_id
                WHERE o.order_status IN ('CONFIRMED', 'DISPATCHED')
                GROUP BY p.category
                ORDER BY category_revenue DESC
                """;
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (DataAccessException e) {
            logger.error("Failed to generate category revenue report", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getDailyRevenueReport() {
        String sql = """
                SELECT DATE(o.created_at) AS sale_date,
                       COUNT(DISTINCT o.order_id) AS orders_count,
                       SUM(oi.quantity) AS items_sold,
                       SUM(oi.subtotal) AS daily_revenue
                FROM orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                WHERE o.order_status IN ('CONFIRMED', 'DISPATCHED')
                GROUP BY DATE(o.created_at)
                ORDER BY sale_date DESC
                """;
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (DataAccessException e) {
            logger.error("Failed to generate daily sales report", e);
            throw e;
        }
    }
}
