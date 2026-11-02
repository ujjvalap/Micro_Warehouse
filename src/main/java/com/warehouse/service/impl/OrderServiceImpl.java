package com.warehouse.service.impl;

import com.warehouse.dao.OrderDao;
import com.warehouse.dao.OrderItemDao;
import com.warehouse.dao.ProductDao;
import com.warehouse.dto.CreateOrderRequest;
import com.warehouse.dto.OrderItemRequest;
import com.warehouse.dto.OrderResult;
import com.warehouse.model.Order;
import com.warehouse.model.OrderItem;
import com.warehouse.model.OrderStatus;
import com.warehouse.model.Product;
import com.warehouse.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service implementation executing multi-item batch order creation (FR-2)
 * and atomic inventory deduction with zero stock corruption (FR-3).
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final ProductDao productDao;

    public OrderServiceImpl(OrderDao orderDao, OrderItemDao orderItemDao, ProductDao productDao) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.productDao = productDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResult createAndProcessOrder(CreateOrderRequest request) {
        long startTime = System.currentTimeMillis();

        if (request == null || request.getCustomerPhone() == null || request.getCustomerPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer phone number is required.");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one order line item is required.");
        }

        String phone = request.getCustomerPhone().trim();

        // 1. Consolidate requested quantities if a SKU appears multiple times
        Map<Integer, Integer> consolidatedItems = new LinkedHashMap<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for SKU ID: " + itemReq.getSkuId());
            }
            consolidatedItems.merge(itemReq.getSkuId(), itemReq.getQuantity(), Integer::sum);
        }

        // 2. Atomic Stock Verification (Row lock via SELECT FOR UPDATE to prevent race conditions)
        Map<Integer, Integer> lockedStock = productDao.findStockForUpdate(consolidatedItems.keySet());
        List<String> rejectionReasons = new ArrayList<>();
        Map<Integer, Product> catalogMap = new LinkedHashMap<>();

        for (Map.Entry<Integer, Integer> entry : consolidatedItems.entrySet()) {
            int skuId = entry.getKey();
            int requestedQty = entry.getValue();

            Optional<Product> prodOpt = productDao.findById(skuId);
            if (prodOpt.isEmpty()) {
                rejectionReasons.add("SKU " + skuId + " does not exist in product catalog.");
                continue;
            }

            Product product = prodOpt.get();
            catalogMap.put(skuId, product);

            int availableQty = lockedStock.getOrDefault(skuId, product.getStockQty());
            if (availableQty < requestedQty) {
                rejectionReasons.add(String.format("Insufficient stock for SKU %d ('%s'): Requested %d, Available %d.",
                        skuId, product.getName(), requestedQty, availableQty));
            }
        }

        // 3. Atomic Decision: If ANY single SKU is insufficient, reject whole order with ZERO stock deduction
        if (!rejectionReasons.isEmpty()) {
            logger.warn("Order for {} REJECTED. Constraints: {}", phone, rejectionReasons);

            Order rejectedOrder = new Order(phone, OrderStatus.REJECTED);
            rejectedOrder = orderDao.createOrder(rejectedOrder);

            long elapsed = System.currentTimeMillis() - startTime;
            return OrderResult.rejected(
                    rejectedOrder,
                    "Order REJECTED: Stock unavailable. Zero warehouse stock was deducted.",
                    rejectionReasons,
                    elapsed
            );
        }

        // 4. All SKUs available: Prepare line items and deduct inventory atomically
        List<OrderItem> orderItemsToPersist = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : consolidatedItems.entrySet()) {
            int skuId = entry.getKey();
            int qty = entry.getValue();
            Product product = catalogMap.get(skuId);

            BigDecimal unitPrice = product.getUnitPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderItem orderItem = new OrderItem();
            orderItem.setSkuId(skuId);
            orderItem.setQuantity(qty);
            orderItem.setSubtotal(subtotal);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(unitPrice);
            orderItemsToPersist.add(orderItem);
        }

        // Batch deduct inventory using SQL safety check stock_qty >= ?
        int[] deductionResults = productDao.deductStockBatch(orderItemsToPersist);
        for (int i = 0; i < deductionResults.length; i++) {
            if (deductionResults[i] == 0) {
                // Stock dropped concurrently
                throw new IllegalStateException("Concurrent stock deduction failed for SKU: " + orderItemsToPersist.get(i).getSkuId());
            }
        }

        // 5. Persist CONFIRMED Order Header
        Order confirmedOrder = new Order(phone, OrderStatus.CONFIRMED);
        confirmedOrder = orderDao.createOrder(confirmedOrder);
        final int orderId = confirmedOrder.getOrderId();

        // 6. Persist all line items via single round-trip JdbcTemplate.batchUpdate() (FR-2)
        for (OrderItem item : orderItemsToPersist) {
            item.setOrderId(orderId);
        }
        orderItemDao.batchInsert(orderId, orderItemsToPersist);
        confirmedOrder.setItems(orderItemsToPersist);

        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("Order #{} CONFIRMED for {} with {} items (Total: Rs.{}) in {}ms",
                orderId, phone, orderItemsToPersist.size(), confirmedOrder.getTotalAmount(), elapsed);

        return OrderResult.confirmed(
                confirmedOrder,
                "Order CONFIRMED: Inventory atomically deducted and line items persisted via batchUpdate().",
                elapsed
        );
    }

    @Override
    public Optional<Order> getOrderById(int orderId) {
        Optional<Order> orderOpt = orderDao.findById(orderId);
        orderOpt.ifPresent(order -> {
            List<OrderItem> items = orderItemDao.findByOrderId(order.getOrderId());
            order.setItems(items);
        });
        return orderOpt;
    }

    @Override
    public List<Order> getAllOrders() {
        List<Order> orders = orderDao.findAll();
        for (Order order : orders) {
            List<OrderItem> items = orderItemDao.findByOrderId(order.getOrderId());
            order.setItems(items);
        }
        return orders;
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderDao.findByStatus(status);
        for (Order order : orders) {
            List<OrderItem> items = orderItemDao.findByOrderId(order.getOrderId());
            order.setItems(items);
        }
        return orders;
    }

    @Override
    public List<Order> getOrdersByCustomerPhone(String customerPhone) {
        List<Order> orders = orderDao.findByCustomerPhone(customerPhone);
        for (Order order : orders) {
            List<OrderItem> items = orderItemDao.findByOrderId(order.getOrderId());
            order.setItems(items);
        }
        return orders;
    }

    @Override
    @Transactional
    public boolean dispatchOrder(int orderId) {
        Optional<Order> orderOpt = orderDao.findById(orderId);
        if (orderOpt.isEmpty()) {
            logger.warn("Cannot dispatch order: orderId {} not found", orderId);
            return false;
        }

        Order order = orderOpt.get();
        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            logger.warn("Order {} cannot be dispatched because its status is: {}", orderId, order.getOrderStatus());
            return false;
        }

        boolean updated = orderDao.updateOrderStatus(orderId, OrderStatus.DISPATCHED);
        if (updated) {
            logger.info("Order {} transitioned to DISPATCHED for quick-commerce delivery", orderId);
        }
        return updated;
    }
}
