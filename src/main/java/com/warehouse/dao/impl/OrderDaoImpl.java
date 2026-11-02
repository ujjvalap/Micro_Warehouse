package com.warehouse.dao.impl;

import com.warehouse.dao.OrderDao;
import com.warehouse.model.Order;
import com.warehouse.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring JdbcTemplate implementation of OrderDao.
 * Uses KeyHolder to retrieve generated order_id.
 */
@Repository
public class OrderDaoImpl implements OrderDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public OrderDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        int orderId = rs.getInt("order_id");
        String phone = rs.getString("customer_phone");
        String statusStr = rs.getString("order_status");
        Timestamp createdAtTs = rs.getTimestamp("created_at");

        OrderStatus status = OrderStatus.valueOf(statusStr);
        LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : LocalDateTime.now();

        return new Order(orderId, phone, status, createdAt);
    };

    @Override
    public Order createOrder(Order order) {
        String sql = "INSERT INTO orders (customer_phone, order_status, created_at) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, order.getCustomerPhone());
                ps.setString(2, order.getOrderStatus().name());
                ps.setTimestamp(3, Timestamp.valueOf(now));
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                int generatedId = keyHolder.getKey().intValue();
                order.setOrderId(generatedId);
            }
            order.setCreatedAt(now);
            return order;
        } catch (DataAccessException e) {
            logger.error("Failed to insert order for customer: {}", order.getCustomerPhone(), e);
            throw e;
        }
    }

    @Override
    public Optional<Order> findById(int orderId) {
        String sql = "SELECT order_id, customer_phone, order_status, created_at FROM orders WHERE order_id = ?";
        try {
            List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, orderId);
            return orders.stream().findFirst();
        } catch (DataAccessException e) {
            logger.error("Failed to find order by ID: {}", orderId, e);
            throw e;
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT order_id, customer_phone, order_status, created_at FROM orders ORDER BY order_id DESC";
        try {
            return jdbcTemplate.query(sql, orderRowMapper);
        } catch (DataAccessException e) {
            logger.error("Failed to query all orders", e);
            throw e;
        }
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        String sql = "SELECT order_id, customer_phone, order_status, created_at FROM orders WHERE order_status = ? ORDER BY order_id DESC";
        try {
            return jdbcTemplate.query(sql, orderRowMapper, status.name());
        } catch (DataAccessException e) {
            logger.error("Failed to find orders by status: {}", status, e);
            throw e;
        }
    }

    @Override
    public List<Order> findByCustomerPhone(String customerPhone) {
        String sql = "SELECT order_id, customer_phone, order_status, created_at FROM orders WHERE customer_phone = ? ORDER BY order_id DESC";
        try {
            return jdbcTemplate.query(sql, orderRowMapper, customerPhone);
        } catch (DataAccessException e) {
            logger.error("Failed to find orders by phone: {}", customerPhone, e);
            throw e;
        }
    }

    @Override
    public boolean updateOrderStatus(int orderId, OrderStatus newStatus) {
        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try {
            int rows = jdbcTemplate.update(sql, newStatus.name(), orderId);
            return rows > 0;
        } catch (DataAccessException e) {
            logger.error("Failed to update status for order: {}", orderId, e);
            throw e;
        }
    }
}
