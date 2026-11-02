package com.warehouse.service.impl;

import com.warehouse.dao.ProductDao;
import com.warehouse.model.Product;
import com.warehouse.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for SKU Catalog management and inventory replenishment (FR-1).
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductDao productDao;

    public ProductServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    @Transactional
    public Product addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Product category cannot be empty");
        }
        if (product.getUnitPrice() == null || product.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        if (product.getStockQty() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        int generatedId = productDao.addProduct(product);
        logger.info("Successfully registered SKU {} ('{}') in category '{}'", generatedId, product.getName(), product.getCategory());
        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    @Override
    public Optional<Product> getProductById(int skuId) {
        if (skuId <= 0) {
            throw new IllegalArgumentException("SKU ID must be positive: " + skuId);
        }
        return productDao.findById(skuId);
    }

    @Override
    @Transactional
    public Product updateProduct(Product product) {
        if (product == null || product.getSkuId() <= 0) {
            throw new IllegalArgumentException("Valid SKU ID is required for update");
        }

        Optional<Product> existing = productDao.findById(product.getSkuId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Product with SKU ID " + product.getSkuId() + " does not exist");
        }

        productDao.updateProduct(product);
        logger.info("Updated product SKU {}: price={}, category={}", product.getSkuId(), product.getUnitPrice(), product.getCategory());
        return product;
    }

    @Override
    @Transactional
    public void replenishStock(int skuId, int quantity) {
        if (skuId <= 0) {
            throw new IllegalArgumentException("SKU ID must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Replenish quantity must be greater than zero");
        }

        Optional<Product> productOpt = productDao.findById(skuId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found with SKU ID: " + skuId);
        }

        int updated = productDao.replenishStock(skuId, quantity);
        if (updated == 0) {
            throw new IllegalStateException("Failed to replenish stock for SKU: " + skuId);
        }

        logger.info("Replenished {} units for SKU {} ('{}'). New stock: {}",
                quantity, skuId, productOpt.get().getName(), productOpt.get().getStockQty() + quantity);
    }
}
