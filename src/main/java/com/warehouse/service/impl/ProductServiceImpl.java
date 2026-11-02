
package com.warehouse.service.impl;

import com.warehouse.dao.ProductDao;
import com.warehouse.model.Product;
import com.warehouse.service.ProductService;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;

    // Constructor Injection
    public ProductServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    // 1. Add a new product
    @Override
    public void addProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("Product category is required");
        }

        if (product.getUnitPrice() == null ||
                product.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }

        if (product.getStockQty() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        int rowsAffected = productDao.addProduct(product);

        if (rowsAffected != 1) {
            throw new IllegalStateException("Product could not be added");
        }
    }

    // 2. View all products
    @Override
    public List<Product> getAllProducts() {

        return productDao.getAllProducts();
    }

    // 3. Search product using SKU ID
    @Override
    public Product getProductById(int skuId) {

        if (skuId <= 0) {
            throw new IllegalArgumentException("SKU ID must be positive");
        }

        Product product = productDao.getProductById(skuId);

        if (product == null) {
            throw new IllegalArgumentException("Product not found with SKU ID: " + skuId);
        }

        return product;
    }

    // 4. Update product price and category
    @Override
    public void updateProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (product.getSkuId() <= 0) {
            throw new IllegalArgumentException("Invalid SKU ID");
        }

        if (product.getUnitPrice() == null ||
                product.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }

        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("Product category is required");
        }

        // Check whether the product exists
        getProductById(product.getSkuId());

        int rowsAffected = productDao.updateProduct(product);

        if (rowsAffected == 0) {
            throw new IllegalStateException("Product update was not successful");
        }
    }

    // 5. Replenish existing stock
    @Override
    public void replenishStock(int skuId, int quantity) {

        if (skuId <= 0) {
            throw new IllegalArgumentException("Invalid SKU ID");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Replenishment quantity must be positive");
        }

        // Check whether the product exists
        getProductById(skuId);

        int rowsAffected = productDao.replenishStock(skuId, quantity);

        if (rowsAffected == 0) {
            throw new IllegalStateException("Stock replenishment was not successful");
        }
    }
}
