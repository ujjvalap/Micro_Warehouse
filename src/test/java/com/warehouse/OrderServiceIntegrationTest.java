package com.warehouse;

import com.warehouse.dto.CreateOrderRequest;
import com.warehouse.dto.OrderItemRequest;
import com.warehouse.dto.OrderResult;
import com.warehouse.model.Order;
import com.warehouse.model.OrderStatus;
import com.warehouse.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "interactive.cli=false"
})
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void resetStock() {
        // Reset known test product quantities
        jdbcTemplate.update("UPDATE products SET stock_qty = 50 WHERE sku_id = 1"); // Amul Milk
        jdbcTemplate.update("UPDATE products SET stock_qty = 40 WHERE sku_id = 2"); // Nandini Curd
        jdbcTemplate.update("UPDATE products SET stock_qty = 8 WHERE sku_id = 7");  // Fresh Coriander (Low stock)
    }

    @Test
    @DisplayName("FR-2 & FR-3: Multi-Item Batch Order Creation with Atomic Stock Deduction")
    public void testCreateAndProcessOrder_Success() {
        int initialStockSku1 = getProductStock(1);
        int initialStockSku2 = getProductStock(2);

        CreateOrderRequest request = new CreateOrderRequest("9876543210", List.of(
                new OrderItemRequest(1, 3), // 3 x 54.00 = 162.00
                new OrderItemRequest(2, 2)  // 2 x 32.00 = 64.00
        ));

        OrderResult result = orderService.createAndProcessOrder(request);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertNotNull(result.getOrder());

        int orderId = result.getOrder().getOrderId();
        assertTrue(orderId > 0);

        // Verify stock deducted accurately
        assertEquals(initialStockSku1 - 3, getProductStock(1));
        assertEquals(initialStockSku2 - 2, getProductStock(2));

        // Verify line items persisted via batchUpdate()
        Optional<Order> fetchedOrder = orderService.getOrderById(orderId);
        assertTrue(fetchedOrder.isPresent());
        Order order = fetchedOrder.get();
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("226.00"), order.getTotalAmount());
    }

    @Test
    @DisplayName("FR-3: Atomic Inventory Deduction - Order Rejection with Zero Stock Corruption")
    public void testCreateAndProcessOrder_AtomicRejection_NoStockDeducted() {
        int initialStockSku1 = getProductStock(1); // 50 available
        int initialStockSku7 = getProductStock(7); // 8 available

        // Item 1 has sufficient stock (5 units), but Item 2 requests 50 units (only 8 in stock)
        CreateOrderRequest request = new CreateOrderRequest("9991112233", List.of(
                new OrderItemRequest(1, 5),
                new OrderItemRequest(7, 50) // Exceeds stock!
        ));

        OrderResult result = orderService.createAndProcessOrder(request);

        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertEquals(OrderStatus.REJECTED, result.getStatus());
        assertFalse(result.getRejectionReasons().isEmpty());
        assertTrue(result.getRejectionReasons().get(0).contains("Insufficient stock for SKU 7"));

        // CRITICAL: Stock for SKU 1 must remain 100% UNCHANGED (Atomic consistency)
        assertEquals(initialStockSku1, getProductStock(1), "Stock for SKU 1 must not be deducted on partial order failure!");
        assertEquals(initialStockSku7, getProductStock(7), "Stock for SKU 7 must remain unchanged!");
    }

    @Test
    @DisplayName("Order Dispatch: Transition CONFIRMED to DISPATCHED")
    public void testDispatchOrder() {
        CreateOrderRequest request = new CreateOrderRequest("9811223344", List.of(
                new OrderItemRequest(1, 1)
        ));
        OrderResult result = orderService.createAndProcessOrder(request);
        assertTrue(result.isSuccessful());

        int orderId = result.getOrder().getOrderId();
        boolean dispatched = orderService.dispatchOrder(orderId);
        assertTrue(dispatched);

        Optional<Order> dispatchedOrder = orderService.getOrderById(orderId);
        assertTrue(dispatchedOrder.isPresent());
        assertEquals(OrderStatus.DISPATCHED, dispatchedOrder.get().getOrderStatus());

        // Attempting to dispatch again should fail
        boolean redispatch = orderService.dispatchOrder(orderId);
        assertFalse(redispatch);
    }

    private int getProductStock(int skuId) {
        Integer stock = jdbcTemplate.queryForObject(
                "SELECT stock_qty FROM products WHERE sku_id = ?",
                Integer.class,
                skuId
        );
        return stock != null ? stock : 0;
    }
}
