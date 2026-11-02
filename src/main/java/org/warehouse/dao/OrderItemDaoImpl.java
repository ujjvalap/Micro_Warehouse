package org.warehouse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.warehouse.model.OrderItem;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of OrderItemDao utilizing Spring JdbcTemplate batchUpdate()
 * for efficient single-round-trip batch order item persistence.
 */
@Repository
public class OrderItemDaoImpl implements OrderItemDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderItemDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final OrderItemRowMapper orderItemRowMapper = new OrderItemRowMapper();

    public OrderItemDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int[] batchInsert(int orderId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return new int[0];
        }

        String sql = "INSERT INTO order_items (order_id, sku_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
        try {
            logger.info("Executing batchUpdate for {} order items on orderId: {}", items.size(), orderId);
            return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
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
        } catch (DataAccessException e) {
            logger.error("Failed to batch insert order items for orderId: {}", orderId, e);
            throw e;
        }
    }

    @Override
    public List<OrderItem> findByOrderId(int orderId) {
        // Parameterized SQL query joining with products table to enrich item display
        String sql = "SELECT oi.item_id, oi.order_id, oi.sku_id, oi.quantity, oi.subtotal, " +
                     "p.name AS product_name, p.unit_price " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.sku_id = p.sku_id " +
                     "WHERE oi.order_id = ? " +
                     "ORDER BY oi.item_id ASC";
        try {
            return jdbcTemplate.query(sql, orderItemRowMapper, orderId);
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving order items for orderId: {}", orderId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Custom RowMapper implementation for OrderItem entities.
     */
    public static class OrderItemRowMapper implements RowMapper<OrderItem> {
        @Override
        public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrderItem item = new OrderItem();
            item.setItemId(rs.getInt("item_id"));
            item.setOrderId(rs.getInt("order_id"));
            item.setSkuId(rs.getInt("sku_id"));
            item.setQuantity(rs.getInt("quantity"));
            item.setSubtotal(rs.getBigDecimal("subtotal"));

            // Enriched joined fields if available
            try {
                item.setProductName(rs.getString("product_name"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
            } catch (SQLException ignored) {
                // Ignore if query did not select these columns
            }
            return item;
        }
    }
}
