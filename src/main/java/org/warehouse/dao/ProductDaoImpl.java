package org.warehouse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.warehouse.model.Product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProductDao using Spring JdbcTemplate,
 * custom RowMapper, and parameterized SQL queries.
 */
@Repository
public class ProductDaoImpl implements ProductDao {

    private static final Logger logger = LoggerFactory.getLogger(ProductDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final ProductRowMapper productRowMapper = new ProductRowMapper();

    public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Product> findById(int skuId) {
        String sql = "SELECT sku_id, name, category, unit_price, stock_qty FROM products WHERE sku_id = ?";
        try {
            Product product = jdbcTemplate.queryForObject(sql, productRowMapper, skuId);
            return Optional.ofNullable(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            logger.error("Database error while fetching product by skuId: {}", skuId, e);
            throw e;
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT sku_id, name, category, unit_price, stock_qty FROM products ORDER BY category, name";
        try {
            return jdbcTemplate.query(sql, productRowMapper);
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving all products", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Product> findLowStockProducts(int threshold) {
        String sql = "SELECT sku_id, name, category, unit_price, stock_qty FROM products WHERE stock_qty < ? ORDER BY stock_qty ASC";
        try {
            return jdbcTemplate.query(sql, productRowMapper, threshold);
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving low stock products below threshold: {}", threshold, e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean deductStock(int skuId, int quantity) {
        // Atomic update preventing race condition: deducts only if stock_qty >= quantity
        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE sku_id = ? AND stock_qty >= ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, quantity, skuId, quantity);
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            logger.error("Database error while deducting {} units from skuId: {}", quantity, skuId, e);
            throw e;
        }
    }

    @Override
    public boolean replenishStock(int skuId, int quantity) {
        String sql = "UPDATE products SET stock_qty = stock_qty + ? WHERE sku_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, quantity, skuId);
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            logger.error("Database error while replenishing {} units to skuId: {}", quantity, skuId, e);
            throw e;
        }
    }

    /**
     * Custom RowMapper implementation for Product entities.
     */
    public static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product product = new Product();
            product.setSkuId(rs.getInt("sku_id"));
            product.setName(rs.getString("name"));
            product.setCategory(rs.getString("category"));
            product.setUnitPrice(rs.getBigDecimal("unit_price"));
            product.setStockQty(rs.getInt("stock_qty"));
            return product;
        }
    }
}
