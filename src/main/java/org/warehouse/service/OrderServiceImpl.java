package org.warehouse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.warehouse.dao.OrderDao;
import org.warehouse.dao.OrderItemDao;
import org.warehouse.dao.ProductDao;
import org.warehouse.dto.CreateOrderRequest;
import org.warehouse.dto.OrderItemRequest;
import org.warehouse.dto.OrderResult;
import org.warehouse.model.Order;
import org.warehouse.model.OrderItem;
import org.warehouse.model.OrderStatus;
import org.warehouse.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service implementation executing multi-item batch order creation
 * and atomic inventory validation and deduction.
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
            throw new IllegalArgumentException("Customer phone number is required to place an order.");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one order line item is required.");
        }

        String phone = request.getCustomerPhone().trim();

        // Step 1: Consolidate quantities if the same SKU appears multiple times
        Map<Integer, Integer> consolidatedItems = new LinkedHashMap<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for SKU ID: " + itemReq.getSkuId());
            }
            consolidatedItems.merge(itemReq.getSkuId(), itemReq.getQuantity(), Integer::sum);
        }

        // Step 2: Atomic Inventory Pre-Check (Verify every SKU exists and has sufficient stock)
        List<String> rejectionReasons = new ArrayList<>();
        Map<Integer, Product> verifiedProducts = new LinkedHashMap<>();

        for (Map.Entry<Integer, Integer> entry : consolidatedItems.entrySet()) {
            int skuId = entry.getKey();
            int requestedQty = entry.getValue();

            Optional<Product> productOpt = productDao.findById(skuId);
            if (productOpt.isEmpty()) {
                rejectionReasons.add("SKU ID " + skuId + " does not exist in catalog.");
                continue;
            }

            Product product = productOpt.get();
            verifiedProducts.put(skuId, product);

            if (product.getStockQty() < requestedQty) {
                rejectionReasons.add(String.format("Insufficient stock for SKU %d ('%s'): Requested %d, Available %d.",
                        skuId, product.getName(), requestedQty, product.getStockQty()));
            }
        }

        // Step 3: Atomic Evaluation
        // If any single SKU is insufficient, reject the order without deducting ANY stock
        if (!rejectionReasons.isEmpty()) {
            logger.warn("Order for customer {} REJECTED due to {} stock constraint(s): {}",
                    phone, rejectionReasons.size(), rejectionReasons);

            Order rejectedOrder = new Order(phone, OrderStatus.REJECTED);
            rejectedOrder = orderDao.createOrder(rejectedOrder);

            long elapsed = System.currentTimeMillis() - startTime;
            return OrderResult.rejected(
                    rejectedOrder,
                    "Order REJECTED: Stock unavailable. Zero inventory was deducted.",
                    rejectionReasons,
                    elapsed
            );
        }

        // Step 4: All SKUs in stock -> Atomic Inventory Deduction
        List<OrderItem> orderItemsToPersist = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : consolidatedItems.entrySet()) {
            int skuId = entry.getKey();
            int qtyToDeduct = entry.getValue();
            Product product = verifiedProducts.get(skuId);

            boolean deducted = productDao.deductStock(skuId, qtyToDeduct);
            if (!deducted) {
                // Highly concurrent edge case where stock dropped in microsecond gap
                logger.error("Concurrent stock deduction conflict for SKU {}", skuId);
                throw new IllegalStateException("Concurrent inventory deduction conflict for SKU: " + skuId);
            }

            BigDecimal unitPrice = product.getUnitPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qtyToDeduct));

            OrderItem orderItem = new OrderItem();
            orderItem.setSkuId(skuId);
            orderItem.setQuantity(qtyToDeduct);
            orderItem.setSubtotal(subtotal);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(unitPrice);
            orderItemsToPersist.add(orderItem);
        }

        // Step 5: Persist CONFIRMED Order Header
        Order confirmedOrder = new Order(phone, OrderStatus.CONFIRMED);
        confirmedOrder = orderDao.createOrder(confirmedOrder);
        final int orderId = confirmedOrder.getOrderId();

        // Step 6: Single-Trip Batch Persistence via JdbcTemplate.batchUpdate() (FR-2)
        for (OrderItem item : orderItemsToPersist) {
            item.setOrderId(orderId);
        }
        orderItemDao.batchInsert(orderId, orderItemsToPersist);
        confirmedOrder.setItems(orderItemsToPersist);

        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("Order {} successfully CONFIRMED for customer {} with {} items (Total: Rs.{}) in {}ms",
                orderId, phone, orderItemsToPersist.size(), confirmedOrder.getTotalAmount(), elapsed);

        return OrderResult.confirmed(
                confirmedOrder,
                "Order CONFIRMED: Inventory atomically deducted and " + orderItemsToPersist.size() +
                        " line items persisted via batchUpdate().",
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
            logger.info("Order {} successfully updated to DISPATCHED for quick-commerce delivery", orderId);
        }
        return updated;
    }

    @Override
    public List<Product> getAllProducts() {
        return productDao.findAll();
    }
}
