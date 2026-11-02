package com.warehouse.service.impl;

import com.warehouse.dao.ProductDao;
import com.warehouse.model.Product;
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
class RestockServiceImplTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private RestockServiceImpl restockService;

    @Test
    void testGetLowStockProducts() {
        Product lowStockProduct = new Product(7, "Fresh Coriander", "Produce", new BigDecimal("15.00"), 8);
        when(productDao.findLowStockProducts(10)).thenReturn(List.of(lowStockProduct));

        List<Product> results = restockService.getLowStockProducts(10);

        assertEquals(1, results.size());
        assertEquals("Fresh Coriander", results.get(0).getName());
        assertEquals(8, results.get(0).getStockQty());
    }

    @Test
    void testReplenishStock() {
        Product product = new Product(7, "Fresh Coriander", "Produce", new BigDecimal("15.00"), 8);
        when(productDao.findById(7)).thenReturn(Optional.of(product));
        when(productDao.replenishStock(7, 25)).thenReturn(1);

        assertDoesNotThrow(() -> restockService.replenishStock(7, 25));
        verify(productDao).replenishStock(7, 25);
    }
}
