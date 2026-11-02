
package com.warehouse.dao.impl;

import com.warehouse.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
                "Laptop",
                "Electronics",
                new BigDecimal("55000.00"),
                10
        );
    }

    @Test
    void testAddProduct() {

        when(jdbcTemplate.update(
                anyString(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(1);

        int result = productDao.addProduct(product);

        assertEquals(1, result);

        verify(jdbcTemplate, times(1)).update(
                anyString(),
                eq("Laptop"),
                eq("Electronics"),
                eq(new BigDecimal("55000.00")),
                eq(10)
        );
    }

    @Test
    void testGetAllProducts() {

        List<Product> expectedProducts = List.of(product);

        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<Product>>any()
        )).thenReturn(expectedProducts);

        List<Product> actualProducts = productDao.getAllProducts();

        assertEquals(1, actualProducts.size());
        assertEquals("Laptop", actualProducts.get(0).getName());
    }

    @Test
    void testGetProductById() {

        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<Product>>any(),
                eq(1)
        )).thenReturn(List.of(product));

        Product actualProduct = productDao.getProductById(1);

        assertNotNull(actualProduct);
        assertEquals(1, actualProduct.getSkuId());
        assertEquals("Laptop", actualProduct.getName());
    }

    @Test
    void testGetProductByIdWhenNotFound() {

        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<Product>>any(),
                eq(99)
        )).thenReturn(List.of());

        Product actualProduct = productDao.getProductById(99);

        assertNull(actualProduct);
    }

    @Test
    void testUpdateProduct() {

        when(jdbcTemplate.update(
                anyString(),
                any(),
                any(),
                any()
        )).thenReturn(1);

        product.setCategory("Computers");
        product.setUnitPrice(new BigDecimal("60000.00"));

        int result = productDao.updateProduct(product);

        assertEquals(1, result);

        verify(jdbcTemplate).update(
                anyString(),
                eq(new BigDecimal("60000.00")),
                eq("Computers"),
                eq(1)
        );
    }

    @Test
    void testReplenishStock() {

        when(jdbcTemplate.update(
                anyString(),
                eq(5),
                eq(1)
        )).thenReturn(1);

        int result = productDao.replenishStock(1, 5);

        assertEquals(1, result);

        verify(jdbcTemplate).update(
                anyString(),
                eq(5),
                eq(1)
        );
    }
}
