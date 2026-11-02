
package com.example.warehouse.service.impl;

import com.example.warehouse.dao.RestockDao;
import com.example.warehouse.model.Product;
import com.example.warehouse.service.RestockService;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestockServiceImpl implements RestockService {

    private final RestockDao restockDao;

    // Constructor Injection
    public RestockServiceImpl(RestockDao restockDao) {
        this.restockDao = restockDao;
    }

    // Get products with low stock
    @Override
    public List<Product> getLowStockProducts(int threshold) {

        if (threshold <= 0) {
            throw new IllegalArgumentException(
                    "Threshold must be greater than zero"
            );
        }

        try {
            return restockDao.findLowStockProducts(threshold);

        } catch (DataAccessException e) {
            throw new RuntimeException(
                    "Unable to fetch low stock products", e
            );
        }
    }

    // Replenish existing product stock
    @Override
    public void replenishStock(int skuId, int quantity) {

        if (skuId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid SKU ID"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        try {

            Product product = restockDao.findProductById(skuId);

            if (product == null) {
                throw new IllegalArgumentException(
                        "Product not found with SKU ID: " + skuId
                );
            }

            int rowsUpdated = restockDao.updateStock(skuId, quantity);

            if (rowsUpdated == 0) {
                throw new RuntimeException(
                        "Stock update failed"
                );
            }

            System.out.println(
                    "Stock successfully replenished for: "
                            + product.getName()
            );

        } catch (DataAccessException e) {

            throw new RuntimeException(
                    "Database error while replenishing stock", e
            );
        }
    }
}
