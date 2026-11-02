
package com.warehouse.dao.impl;

import com.warehouse.dao.ProductDao;
import com.warehouse.model.Product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductDaoImpl implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    // Constructor Injection
    public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper: Converts database rows into Product objects
    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> {
        Product product = new Product();

        product.setSkuId(rs.getInt("sku_id"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setUnitPrice(rs.getBigDecimal("unit_price"));
        product.setStockQty(rs.getInt("stock_qty"));

        return product;
    };

    // 1. Add a new product
    @Override
    public int addProduct(Product product) {

        String sql = """
                INSERT INTO products
                (name, category, unit_price, stock_qty)
                VALUES (?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                product.getName(),
                product.getCategory(),
                product.getUnitPrice(),
                product.getStockQty()
        );
    }

    // 2. Retrieve all products
    @Override
    public List<Product> getAllProducts() {

        String sql = "SELECT * FROM products";

        return jdbcTemplate.query(sql, productRowMapper);
    }

    // 3. Retrieve product using SKU ID
    @Override
    public Product getProductById(int skuId) {

        String sql = "SELECT * FROM products WHERE sku_id = ?";

        List<Product> products = jdbcTemplate.query(
                sql,
                productRowMapper,
                skuId
        );

        return products.isEmpty() ? null : products.get(0);
    }

    // 4. Update product price and category
    @Override
    public int updateProduct(Product product) {

        String sql = """
                UPDATE products
                SET unit_price = ?, category = ?
                WHERE sku_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                product.getUnitPrice(),
                product.getCategory(),
                product.getSkuId()
        );
    }

    // 5. Replenish existing product stock
    @Override
    public int replenishStock(int skuId, int quantity) {

        String sql = """
                UPDATE products
                SET stock_qty = stock_qty + ?
                WHERE sku_id = ?
                """;

        return jdbcTemplate.update(sql, quantity, skuId);
    }
}
