package com.warehouse.dao.impl;

import com.warehouse.dao.ProductDao;
import com.warehouse.model.OrderItem;
import com.warehouse.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Spring JdbcTemplate implementation of ProductDao.
 * Uses parameterized queries, safe RowMapper extraction, and row-level locking.
 */
@Repository
public class ProductDaoImpl implements ProductDao {

    private static final Logger logger = LoggerFactory.getLogger(ProductDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> new Product(
            rs.getInt("sku_id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getBigDecimal("unit_price"),
            rs.getInt("stock_qty")
    );

    @Override
    public int addProduct(Product product) {
        String sql = "INSERT INTO products (name, category, unit_price, stock_qty) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, product.getName());
                ps.setString(2, product.getCategory());
                ps.setBigDecimal(3, product.getUnitPrice());
                ps.setInt(4, product.getStockQty());
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                int generatedId = keyHolder.getKey().intValue();
                product.setSkuId(generatedId);
                return generatedId;
            }
            return 1;
        } catch (DataAccessException e) {
            logger.error("Failed to insert product: {}", product.getName(), e);
            throw e;
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT sku_id, name, category, unit_price, stock_qty FROM products ORDER BY sku_id ASC";
        try {
            return jdbcTemplate.query(sql, productRowMapper);
        } catch (DataAccessException e) {
            logger.error("Failed to query all products", e);
            throw e;
        }
    }

    @Override
    public Optional<Product> findById(int skuId) {
        String sql = "SELECT sku_id, name, category, unit_price, stock_qty FROM products WHERE sku_id = ?";
        try {
            List<Product> results = jdbcTemplate.query(sql, productRowMapper, skuId);
            return results.stream().findFirst();
        } catch (DataAccessException e) {
            logger.error("Failed to query product by skuId: {}", skuId, e);
            throw e;
        }
    }

    @Override
    public int updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, category = ?, unit_price = ? WHERE sku_id = ?";
        try {
            return jdbcTemplate.update(sql,
                    product.getName(),
                    product.getCategory(),
                    product.getUnitPrice(),
                    product.getSkuId());
        } catch (DataAccessException e) {
            logger.error("Failed to update product SKU: {}", product.getSkuId(), e);
            throw e;
        }
    }

    @Override
    public int replenishStock(int skuId, int quantity) {
        String sql = "UPDATE products SET stock_qty = stock_qty + ? WHERE sku_id = ?";
        try {
            return jdbcTemplate.update(sql, quantity, skuId);
        } catch (DataAccessException e) {
            logger.error("Failed to replenish stock for SKU: {}", skuId, e);
            throw e;
        }
    }

    @Override
    public boolean deductStock(int skuId, int quantity) {
        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE sku_id = ? AND stock_qty >= ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, quantity, skuId, quantity);
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            logger.error("Failed to deduct stock for SKU: {}", skuId, e);
            throw e;
        }
    }

    @Override
    public Map<Integer, Integer> findStockForUpdate(Collection<Integer> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = String.join(",", Collections.nCopies(skuIds.size(), "?"));
        String sql = "SELECT sku_id, stock_qty FROM products WHERE sku_id IN (" + placeholders + ") FOR UPDATE";

        try {
            return jdbcTemplate.query(sql, rs -> {
                Map<Integer, Integer> stockMap = new HashMap<>();
                while (rs.next()) {
                    stockMap.put(rs.getInt("sku_id"), rs.getInt("stock_qty"));
                }
                return stockMap;
            }, skuIds.toArray());
        } catch (DataAccessException e) {
            logger.error("Failed to lock products for update", e);
            throw e;
        }
    }

    @Override
    public int[] deductStockBatch(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return new int[0];
        }

        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE sku_id = ? AND stock_qty >= ?";
        try {
            return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    OrderItem item = items.get(i);
                    ps.setInt(1, item.getQuantity());
                    ps.setInt(2, item.getSkuId());
                    ps.setInt(3, item.getQuantity());
                }

                @Override
                public int getBatchSize() {
                    return items.size();
                }
            });
        } catch (DataAccessException e) {
            logger.error("Failed batch inventory deduction", e);
            throw e;
        }
    }

    @Override
    public List<Product> findLowStockProducts(int threshold) {
        String sql = "SELECT sku_id, name, category, unit_price, stock_qty FROM products WHERE stock_qty < ? ORDER BY stock_qty ASC";
        try {
            return jdbcTemplate.query(sql, productRowMapper, threshold);
        } catch (DataAccessException e) {
            logger.error("Failed to find low stock products with threshold: {}", threshold, e);
            throw e;
        }
    }
}
