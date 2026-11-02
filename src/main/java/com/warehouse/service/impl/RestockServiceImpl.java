package com.warehouse.service.impl;

import com.warehouse.dao.ProductDao;
import com.warehouse.model.Product;
import com.warehouse.service.RestockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for automated restock threshold monitoring and rapid replenishment (FR-4).
 */
@Service
public class RestockServiceImpl implements RestockService {

    private static final Logger logger = LoggerFactory.getLogger(RestockServiceImpl.class);

    private final ProductDao productDao;

    public RestockServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public List<Product> getLowStockProducts(int threshold) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("Restock threshold must be greater than zero");
        }

        try {
            List<Product> lowStock = productDao.findLowStockProducts(threshold);
            logger.info("Found {} products below threshold {}", lowStock.size(), threshold);
            return lowStock;
        } catch (DataAccessException e) {
            logger.error("Database error fetching low stock products", e);
            throw new RuntimeException("Unable to fetch low stock alerts", e);
        }
    }

    @Override
    @Transactional
    public void replenishStock(int skuId, int quantity) {
        if (skuId <= 0) {
            throw new IllegalArgumentException("Invalid SKU ID: " + skuId);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Replenishment quantity must be greater than zero");
        }

        Optional<Product> productOpt = productDao.findById(skuId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found with SKU ID: " + skuId);
        }

        int rows = productDao.replenishStock(skuId, quantity);
        if (rows == 0) {
            throw new RuntimeException("Stock replenishment update failed for SKU: " + skuId);
        }

        logger.info("Restocked SKU {} ('{}') with {} units", skuId, productOpt.get().getName(), quantity);
    }
}
