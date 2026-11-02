package com.warehouse.dao.impl;

import com.warehouse.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ProductDaoImpl productDao;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(
                1,
                "Amul Milk",
                "Dairy",
                new BigDecimal("54.00"),
                50
        );
    }

    @Test
    void testGetAllProducts() {
        List<Product> expectedProducts = List.of(product);

        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<Product>>any()
        )).thenReturn(expectedProducts);

        List<Product> actualProducts = productDao.findAll();

        assertEquals(1, actualProducts.size());
        assertEquals("Amul Milk", actualProducts.get(0).getName());
    }

    @Test
    void testGetProductById() {
        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<Product>>any(),
                eq(1)
        )).thenReturn(List.of(product));

        var actualProductOpt = productDao.findById(1);

        assertTrue(actualProductOpt.isPresent());
        assertEquals(1, actualProductOpt.get().getSkuId());
        assertEquals("Amul Milk", actualProductOpt.get().getName());
    }

    @Test
    void testGetProductByIdWhenNotFound() {
        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<Product>>any(),
                eq(99)
        )).thenReturn(List.of());

        var actualProductOpt = productDao.findById(99);

        assertTrue(actualProductOpt.isEmpty());
    }

    @Test
    void testUpdateProduct() {
        when(jdbcTemplate.update(
                anyString(),
                eq("Amul Milk"),
                eq("Dairy"),
                eq(new BigDecimal("58.00")),
                eq(1)
        )).thenReturn(1);

        product.setUnitPrice(new BigDecimal("58.00"));
        int result = productDao.updateProduct(product);

        assertEquals(1, result);
    }

    @Test
    void testReplenishStock() {
        when(jdbcTemplate.update(
                anyString(),
                eq(20),
                eq(1)
        )).thenReturn(1);

        int result = productDao.replenishStock(1, 20);

        assertEquals(1, result);
    }

    @Test
    void testFindLowStockProducts() {
        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<Product>>any(),
                eq(10)
        )).thenReturn(List.of(product));

        List<Product> lowStock = productDao.findLowStockProducts(10);
        assertEquals(1, lowStock.size());
    }
}
