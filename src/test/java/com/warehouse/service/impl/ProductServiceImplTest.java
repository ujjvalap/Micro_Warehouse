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
import java.util.Optional;

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
                "Amul Taaza Milk 1L",
                "Dairy",
                new BigDecimal("54.00"),
                50
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
        when(productDao.findAll()).thenReturn(List.of(product));

        List<Product> products = productService.getAllProducts();

        assertEquals(1, products.size());
        assertEquals("Amul Taaza Milk 1L", products.get(0).getName());
    }

    @Test
    void testGetProductById() {
        when(productDao.findById(1)).thenReturn(Optional.of(product));

        var result = productService.getProductById(1);

        assertTrue(result.isPresent());
        assertEquals("Amul Taaza Milk 1L", result.get().getName());
    }

    @Test
    void testGetProductByIdWhenNotFound() {
        when(productDao.findById(99)).thenReturn(Optional.empty());

        var result = productService.getProductById(99);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateProduct() {
        when(productDao.findById(1)).thenReturn(Optional.of(product));
        when(productDao.updateProduct(product)).thenReturn(1);

        assertDoesNotThrow(() -> productService.updateProduct(product));
        verify(productDao).updateProduct(product);
    }

    @Test
    void testReplenishStock() {
        when(productDao.findById(1)).thenReturn(Optional.of(product));
        when(productDao.replenishStock(1, 5)).thenReturn(1);

        assertDoesNotThrow(() -> productService.replenishStock(1, 5));
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
