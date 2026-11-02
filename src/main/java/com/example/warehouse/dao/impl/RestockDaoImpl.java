
package com.example.warehouse.dao.impl;

import com.example.warehouse.dao.RestockDao;
import com.example.warehouse.model.Product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RestockDaoImpl implements RestockDao {

    private final JdbcTemplate jdbcTemplate;

    // Constructor Injection
    public RestockDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper: Convert database rows into Product objects
    private final RowMapper<Product> productRowMapper =
            (rs, rowNum) -> new Product(
                    rs.getInt("sku_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getBigDecimal("unit_price"),
                    rs.getInt("stock_qty")
            );

    // Find products with low stock
    @Override
    public List<Product> findLowStockProducts(int threshold) {

        String sql = """
                SELECT sku_id, name, category, unit_price, stock_qty
                FROM products
                WHERE stock_qty < ?
                ORDER BY stock_qty ASC
                """;

        return jdbcTemplate.query(sql, productRowMapper, threshold);
    }

    // Replenish existing product stock
    @Override
    public int updateStock(int skuId, int quantity) {

        String sql = """
                UPDATE products
                SET stock_qty = stock_qty + ?
                WHERE sku_id = ?
                """;

        return jdbcTemplate.update(sql, quantity, skuId);
    }

    // Find product using SKU ID
    @Override
    public Product findProductById(int skuId) {

        String sql = """
                SELECT sku_id, name, category, unit_price, stock_qty
                FROM products
                WHERE sku_id = ?
                """;

        List<Product> products = jdbcTemplate.query(
                sql,
                productRowMapper,
                skuId
        );

        return products.isEmpty() ? null : products.get(0);
    }
}
