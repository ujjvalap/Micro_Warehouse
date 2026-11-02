package com.warehouse.service.impl;

import com.warehouse.dao.SalesDao;
import com.warehouse.model.TopMoverReport;
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
class SalesServiceImplTest {

    @Mock
    private SalesDao salesDao;

    @InjectMocks
    private SalesServiceImpl salesService;

    @Test
    void testGetTotalRevenue() {
        when(salesDao.getTotalFulfillmentRevenue()).thenReturn(new BigDecimal("363.00"));

        BigDecimal revenue = salesService.getTotalRevenue();

        assertEquals(new BigDecimal("363.00"), revenue);
        verify(salesDao).getTotalFulfillmentRevenue();
    }

    @Test
    void testGetTopSellingProducts() {
        TopMoverReport report = new TopMoverReport(1, "Amul Milk", "Dairy", 10, new BigDecimal("540.00"));
        when(salesDao.getTopSellingProducts(5)).thenReturn(List.of(report));

        List<TopMoverReport> result = salesService.getTopSellingProducts(5);

        assertEquals(1, result.size());
        assertEquals("Amul Milk", result.get(0).getProductName());
        assertEquals(10, result.get(0).getTotalUnitsSold());
        verify(salesDao).getTopSellingProducts(5);
    }
}
