package org.warehouse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.warehouse.model.Order;
import org.warehouse.model.OrderStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of OrderDao using Spring JdbcTemplate and KeyHolder
 * for generated primary keys and parameterized queries.
 */
@Repository
public class OrderDaoImpl implements OrderDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final OrderRowMapper orderRowMapper = new OrderRowMapper();

    public OrderDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Order createOrder(Order order) {
        String sql = "INSERT INTO orders (customer_phone, order_status, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, order.getCustomerPhone());
                ps.setString(2, order.getOrderStatus().name());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                order.setOrderId(key.intValue());
            }
            logger.info("Created order entity with generated orderId: {}", order.getOrderId());
            return order;
        } catch (DataAccessException e) {
            logger.error("Failed to insert order for customer phone: {}", order.getCustomerPhone(), e);
            throw e;
        }
    }

    @Override
    public boolean updateOrderStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try {
            int rows = jdbcTemplate.update(sql, status.name(), orderId);
            return rows > 0;
        } catch (DataAccessException e) {
            logger.error("Database error while updating status for orderId: {} to {}", orderId, status, e);
            throw e;
        }
    }

    @Override
    public Optional<Order> findById(int orderId) {
        String sql = "SELECT order_id, customer_phone, order_status, created_at FROM orders WHERE order_id = ?";
        try {
            Order order = jdbcTemplate.queryForObject(sql, orderRowMapper, orderId);
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            logger.error("Database error while fetching order by orderId: {}", orderId, e);
            throw e;
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT order_id, customer_phone, order_status, created_at FROM orders ORDER BY created_at DESC";
        try {
            return jdbcTemplate.query(sql, orderRowMapper);
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving all orders", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        String sql = "SELECT order_id, customer_phone, order_status, created_at FROM orders WHERE order_status = ? ORDER BY created_at DESC";
        try {
            return jdbcTemplate.query(sql, orderRowMapper, status.name());
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving orders with status: {}", status, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Order> findByCustomerPhone(String customerPhone) {
        String sql = "SELECT order_id, customer_phone, order_status, created_at FROM orders WHERE customer_phone = ? ORDER BY created_at DESC";
        try {
            return jdbcTemplate.query(sql, orderRowMapper, customerPhone);
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving orders for customerPhone: {}", customerPhone, e);
            return Collections.emptyList();
        }
    }

    /**
     * Custom RowMapper implementation for Order entities.
     */
    public static class OrderRowMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setCustomerPhone(rs.getString("customer_phone"));
            String statusStr = rs.getString("order_status");
            try {
                order.setOrderStatus(OrderStatus.valueOf(statusStr));
            } catch (IllegalArgumentException | NullPointerException e) {
                order.setOrderStatus(OrderStatus.PENDING);
            }
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) {
                order.setCreatedAt(ts.toLocalDateTime());
            }
            return order;
        }
    }
}
