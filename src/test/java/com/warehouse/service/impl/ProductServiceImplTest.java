
package com.warehouse.service.impl;

import com.warehouse.dao.ProductDao;
import com.warehouse.model.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductServiceImpl productService;

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

        when(productDao.addProduct(product)).thenReturn(1);

        assertDoesNotThrow(() -> productService.addProduct(product));

        verify(productDao).addProduct(product);
    }

    @Test
    void testAddProductWithInvalidPrice() {

        product.setUnitPrice(BigDecimal.ZERO);

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.addProduct(product)
        );

        verify(productDao, never()).addProduct(any());
    }

    @Test
    void testGetAllProducts() {

        when(productDao.getAllProducts())
                .thenReturn(List.of(product));

        List<Product> products = productService.getAllProducts();

        assertEquals(1, products.size());
        assertEquals("Laptop", products.get(0).getName());
    }

    @Test
    void testGetProductById() {

        when(productDao.getProductById(1)).thenReturn(product);

        Product result = productService.getProductById(1);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
    }

    @Test
    void testGetProductByIdWhenNotFound() {

        when(productDao.getProductById(99)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.getProductById(99)
        );
    }

    @Test
    void testUpdateProduct() {

        when(productDao.getProductById(1)).thenReturn(product);
        when(productDao.updateProduct(product)).thenReturn(1);

        assertDoesNotThrow(() -> productService.updateProduct(product));

        verify(productDao).updateProduct(product);
    }

    @Test
    void testReplenishStock() {

        when(productDao.getProductById(1)).thenReturn(product);
        when(productDao.replenishStock(1, 5)).thenReturn(1);

        assertDoesNotThrow(
                () -> productService.replenishStock(1, 5)
        );

        verify(productDao).replenishStock(1, 5);
    }

    @Test
    void testReplenishStockWithInvalidQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.replenishStock(1, -5)
        );

        verify(productDao, never()).replenishStock(anyInt(), anyInt());
    }
}
