package com.warehouse.dao.impl;

import com.warehouse.dao.OrderItemDao;
import com.warehouse.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Spring JdbcTemplate implementation for batch persisting order line items (FR-2).
 * Minimizes database round-trips via batchUpdate().
 */
@Repository
public class OrderItemDaoImpl implements OrderItemDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderItemDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public OrderItemDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<OrderItem> itemRowMapper = (rs, rowNum) -> {
        OrderItem item = new OrderItem(
                rs.getInt("item_id"),
                rs.getInt("order_id"),
                rs.getInt("sku_id"),
                rs.getInt("quantity"),
                rs.getBigDecimal("subtotal")
        );
        // Optional joined product details
        try {
            item.setProductName(rs.getString("product_name"));
            item.setUnitPrice(rs.getBigDecimal("unit_price"));
        } catch (SQLException ignored) {
            // Joined fields not present in simple queries
        }
        return item;
    };

    @Override
    public int[] batchInsert(int orderId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return new int[0];
        }

        String sql = "INSERT INTO order_items (order_id, sku_id, quantity, subtotal) VALUES (?, ?, ?, ?)";

        try {
            int[] updateCounts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    OrderItem item = items.get(i);
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getSkuId());
                    ps.setInt(3, item.getQuantity());
                    ps.setBigDecimal(4, item.getSubtotal());
                }

                @Override
                public int getBatchSize() {
                    return items.size();
                }
            });

            logger.info("Successfully persisted {} order items in batch for order #{}", updateCounts.length, orderId);
            return updateCounts;
        } catch (DataAccessException e) {
            logger.error("Failed batch insertion of order items for order #{}", orderId, e);
            throw e;
        }
    }

    @Override
    public List<OrderItem> findByOrderId(int orderId) {
        String sql = """
                SELECT oi.item_id, oi.order_id, oi.sku_id, oi.quantity, oi.subtotal,
                       p.name AS product_name, p.unit_price
                FROM order_items oi
                JOIN products p ON oi.sku_id = p.sku_id
                WHERE oi.order_id = ?
                ORDER BY oi.item_id ASC
                """;
        try {
            return jdbcTemplate.query(sql, itemRowMapper, orderId);
        } catch (DataAccessException e) {
            logger.error("Failed to query items for order #{}", orderId, e);
            throw e;
        }
    }
}
