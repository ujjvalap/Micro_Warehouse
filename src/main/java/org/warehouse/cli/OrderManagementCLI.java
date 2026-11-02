package org.warehouse.cli;

import org.springframework.stereotype.Component;
import org.warehouse.dto.CreateOrderRequest;
import org.warehouse.dto.OrderItemRequest;
import org.warehouse.dto.OrderResult;
import org.warehouse.model.Order;
import org.warehouse.model.OrderItem;
import org.warehouse.model.OrderStatus;
import org.warehouse.model.Product;
import org.warehouse.service.OrderService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive Console CLI for Order Management & Fulfillment Operations
 * in a Quick-Commerce Micro-Warehouse Hub.
 */
@Component
public class OrderManagementCLI {

    private final OrderService orderService;
    private final Scanner scanner = new Scanner(System.in);

    public OrderManagementCLI(OrderService orderService) {
        this.orderService = orderService;
    }

    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("👉 Select an option [0-7]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleViewProducts();
                    break;
                case "2":
                    handlePlaceMultiItemOrder();
                    break;
                case "3":
                    handleViewOrderById();
                    break;
                case "4":
                    handleDispatchOrder();
                    break;
                case "5":
                    handleViewAllOrders();
                    break;
                case "6":
                    handleSearchByCustomerPhone();
                    break;
                case "7":
                    handleRunAtomicIntegrityDemo();
                    break;
                case "0":
                    System.out.println("\n👋 Exiting Micro-Warehouse Order Hub. Thank you!");
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option. Please enter a number between 0 and 7.\n");
            }
        }
    }

    private void printBanner() {
        System.out.println("============================================================================");
        System.out.println("   ⚡ QUICK-COMMERCE 10-MIN DARK STORE: MICRO-WAREHOUSE ORDER HUB ⚡");
        System.out.println("   [Spring JdbcTemplate • Atomic Inventory Deduction • Batch Processing]   ");
        System.out.println("============================================================================");
    }

    private void printMenu() {
        System.out.println("\n----------------- 📦 ORDER MANAGEMENT MENU -----------------");
        System.out.println(" [1] 📋 View Live Product Catalog & Stock Levels");
        System.out.println(" [2] 🛒 Place Multi-Item Order (FR-2 Batch Insert + FR-3 Atomic Stock)");
        System.out.println(" [3] 🔍 Look Up Order by Order ID (Itemized Invoice)");
        System.out.println(" [4] 🚚 Dispatch Order for 10-Min Delivery (CONFIRMED -> DISPATCHED)");
        System.out.println(" [5] 📜 View All Warehouse Orders (Filter by Status)");
        System.out.println(" [6] 📱 Search Order History by Customer Phone");
        System.out.println(" [7] 🧪 Run Atomic Rollback / Consistency Stress Demo");
        System.out.println(" [0] 🚪 Exit Application");
        System.out.println("------------------------------------------------------------");
    }

    private void handleViewProducts() {
        System.out.println("\n=========================== PRODUCT CATALOG ===========================");
        List<Product> products = orderService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products found in the catalog.");
            return;
        }

        System.out.printf("%-6s | %-38s | %-12s | %-10s | %-8s%n",
                "SKU ID", "Product Name", "Category", "Price", "Stock");
        System.out.println("----------------------------------------------------------------------------------");
        for (Product p : products) {
            String stockDisplay = p.getStockQty() < 10 ? p.getStockQty() + " (LOW!)" : String.valueOf(p.getStockQty());
            System.out.printf("%-6d | %-38s | %-12s | Rs.%-7.2f | %-8s%n",
                    p.getSkuId(),
                    truncate(p.getName(), 38),
                    p.getCategory(),
                    p.getUnitPrice(),
                    stockDisplay);
        }
        System.out.println("==================================================================================");
    }

    private void handlePlaceMultiItemOrder() {
        System.out.println("\n================= 🛒 PLACE NEW MULTI-ITEM ORDER =================");
        System.out.print("Enter Customer Phone (e.g., 9876543210): ");
        String phone = scanner.nextLine().trim();
        if (phone.isEmpty()) {
            System.out.println("❌ Customer phone cannot be empty.");
            return;
        }

        List<OrderItemRequest> items = new ArrayList<>();
        System.out.println("Add items to the order. Type 'done' when finished.");

        while (true) {
            System.out.print("Enter SKU ID (or 'done' to complete): ");
            String skuInput = scanner.nextLine().trim();
            if (skuInput.equalsIgnoreCase("done")) {
                break;
            }

            int skuId;
            try {
                skuId = Integer.parseInt(skuInput);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Please enter a valid numerical SKU ID.");
                continue;
            }

            System.out.print("Enter Quantity for SKU " + skuId + ": ");
            String qtyInput = scanner.nextLine().trim();
            int qty;
            try {
                qty = Integer.parseInt(qtyInput);
                if (qty <= 0) {
                    System.out.println("⚠️ Quantity must be greater than 0.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Please enter a valid quantity.");
                continue;
            }

            items.add(new OrderItemRequest(skuId, qty));
            System.out.println("✔️ Added: SKU " + skuId + " x " + qty);
        }

        if (items.isEmpty()) {
            System.out.println("⚠️ Order cancelled. No items were added.");
            return;
        }

        System.out.println("\n⏳ Processing order atomically through Spring JdbcTemplate...");
        CreateOrderRequest request = new CreateOrderRequest(phone, items);
        OrderResult result = orderService.createAndProcessOrder(request);

        printOrderResult(result);
    }

    private void handleViewOrderById() {
        System.out.print("\nEnter Order ID to inspect: ");
        String input = scanner.nextLine().trim();
        try {
            int orderId = Integer.parseInt(input);
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                System.out.println("❌ Order #" + orderId + " not found.");
                return;
            }
            printOrderDetails(orderOpt.get());
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid Order ID. Please enter a valid number.");
        }
    }

    private void handleDispatchOrder() {
        System.out.print("\nEnter Order ID to dispatch: ");
        String input = scanner.nextLine().trim();
        try {
            int orderId = Integer.parseInt(input);
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                System.out.println("❌ Order #" + orderId + " does not exist.");
                return;
            }

            Order order = orderOpt.get();
            if (order.getOrderStatus() == OrderStatus.DISPATCHED) {
                System.out.println("⚠️ Order #" + orderId + " has already been dispatched!");
                return;
            }
            if (order.getOrderStatus() == OrderStatus.REJECTED) {
                System.out.println("❌ Cannot dispatch Order #" + orderId + " because its status is REJECTED.");
                return;
            }

            boolean dispatched = orderService.dispatchOrder(orderId);
            if (dispatched) {
                System.out.println("✅ Order #" + orderId + " DISPATCHED for 10-minute delivery rider pickup!");
            } else {
                System.out.println("❌ Failed to dispatch Order #" + orderId + ".");
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid Order ID.");
        }
    }

    private void handleViewAllOrders() {
        System.out.println("\nSelect Filter:");
        System.out.println(" 1. All Orders");
        System.out.println(" 2. CONFIRMED Orders");
        System.out.println(" 3. REJECTED Orders");
        System.out.println(" 4. DISPATCHED Orders");
        System.out.print("👉 Choice [1-4]: ");
        String filterChoice = scanner.nextLine().trim();

        List<Order> orders;
        switch (filterChoice) {
            case "2":
                orders = orderService.getOrdersByStatus(OrderStatus.CONFIRMED);
                break;
            case "3":
                orders = orderService.getOrdersByStatus(OrderStatus.REJECTED);
                break;
            case "4":
                orders = orderService.getOrdersByStatus(OrderStatus.DISPATCHED);
                break;
            case "1":
            default:
                orders = orderService.getAllOrders();
                break;
        }

        if (orders.isEmpty()) {
            System.out.println("No orders found matching the filter.");
            return;
        }

        System.out.println("\n============================= ORDERS LIST =============================");
        System.out.printf("%-8s | %-15s | %-12s | %-8s | %-12s | %-19s%n",
                "Order ID", "Customer Phone", "Status", "Items", "Total (Rs.)", "Created At");
        System.out.println("---------------------------------------------------------------------------------");
        for (Order o : orders) {
            String timeStr = o.getCreatedAt() != null ? o.getCreatedAt().toString().replace("T", " ") : "N/A";
            if (timeStr.length() > 19) timeStr = timeStr.substring(0, 19);
            System.out.printf("%-8d | %-15s | %-12s | %-8d | %-12.2f | %-19s%n",
                    o.getOrderId(),
                    o.getCustomerPhone(),
                    o.getOrderStatus(),
                    o.getItems().size(),
                    o.getTotalAmount(),
                    timeStr);
        }
        System.out.println("=================================================================================");
    }

    private void handleSearchByCustomerPhone() {
        System.out.print("\nEnter Customer Phone to search: ");
        String phone = scanner.nextLine().trim();
        List<Order> orders = orderService.getOrdersByCustomerPhone(phone);
        if (orders.isEmpty()) {
            System.out.println("No orders found for customer phone: " + phone);
            return;
        }

        System.out.println("\nOrders found for: " + phone);
        for (Order o : orders) {
            printOrderDetails(o);
        }
    }

    /**
     * Demonstrates FR-3 Atomic Inventory Deduction & Zero Corruption:
     * Attempts an order with 2 valid items and 1 item that deliberately exceeds stock.
     * Verifies that the order is rejected and NO stock was deducted for ANY item.
     */
    private void handleRunAtomicIntegrityDemo() {
        System.out.println("\n================= 🧪 ATOMIC CONSISTENCY STRESS DEMO =================");
        System.out.println("Scenario: A customer attempts an order with 2 items:");
        System.out.println("  - Item 1: SKU 1 (Amul Milk) x 2 units [IN STOCK]");
        System.out.println("  - Item 2: SKU 7 (Fresh Coriander) x 9999 units [MASSIVE STOCK SHORTAGE]");
        System.out.println("\nTesting Atomic Rule: If any single SKU is insufficient, the entire order");
        System.out.println("MUST be REJECTED and ZERO inventory deducted from SKU 1 (Amul Milk).");
        System.out.println("----------------------------------------------------------------------");

        // Fetch pre-test stock for SKU 1
        Optional<Product> milkPreOpt = orderService.getAllProducts().stream()
                .filter(p -> p.getSkuId() == 1).findFirst();
        int milkPreStock = milkPreOpt.map(Product::getStockQty).orElse(-1);
        System.out.println("📊 Pre-test SKU 1 (Milk) Stock: " + milkPreStock + " units");

        CreateOrderRequest request = new CreateOrderRequest("9998887776", List.of(
                new OrderItemRequest(1, 2),
                new OrderItemRequest(7, 9999)
        ));

        OrderResult result = orderService.createAndProcessOrder(request);
        printOrderResult(result);

        // Fetch post-test stock for SKU 1
        Optional<Product> milkPostOpt = orderService.getAllProducts().stream()
                .filter(p -> p.getSkuId() == 1).findFirst();
        int milkPostStock = milkPostOpt.map(Product::getStockQty).orElse(-1);
        System.out.println("📊 Post-test SKU 1 (Milk) Stock: " + milkPostStock + " units");

        if (milkPreStock == milkPostStock && !result.isSuccessful()) {
            System.out.println("🎉 VERIFICATION PASSED: Perfect Atomic Integrity!");
            System.out.println("   No stock was deducted for Item 1 even though Item 1 had available inventory.");
            System.out.println("   Warehouse inventory consistency is 100% maintained!");
        } else {
            System.out.println("⚠️ VERIFICATION FAILED: Inconsistency detected.");
        }
        System.out.println("======================================================================");
    }

    private void printOrderResult(OrderResult result) {
        System.out.println("\n-------------------------- ORDER RESULT --------------------------");
        if (result.isSuccessful()) {
            System.out.println(" Status: 🟢 CONFIRMED");
            System.out.println(" Message: " + result.getMessage());
            System.out.println(" Batch Persistence: Single round-trip via JdbcTemplate.batchUpdate()");
            System.out.println(" Execution Time: " + result.getExecutionTimeMs() + " ms");
            if (result.getOrder() != null) {
                printOrderDetails(result.getOrder());
            }
        } else {
            System.out.println(" Status: 🔴 REJECTED");
            System.out.println(" Message: " + result.getMessage());
            System.out.println(" Execution Time: " + result.getExecutionTimeMs() + " ms");
            if (result.getOrder() != null) {
                System.out.println(" Order Reference ID: #" + result.getOrder().getOrderId());
            }
            System.out.println(" Rejection Reason(s):");
            for (String reason : result.getRejectionReasons()) {
                System.out.println("   • " + reason);
            }
        }
        System.out.println("------------------------------------------------------------------");
    }

    private void printOrderDetails(Order order) {
        System.out.println("\n======================== ORDER #" + order.getOrderId() + " DETAILS ========================");
        System.out.printf("Customer Phone : %s%n", order.getCustomerPhone());
        System.out.printf("Status         : %s%n", order.getOrderStatus());
        System.out.printf("Placed At      : %s%n", order.getCreatedAt());
        System.out.println("Line Items:");
        if (order.getItems() == null || order.getItems().isEmpty()) {
            System.out.println("  (No line items persisted - Order was " + order.getOrderStatus() + ")");
        } else {
            System.out.printf("  %-6s | %-32s | %-6s | %-10s | %-10s%n",
                    "SKU ID", "Product Name", "Qty", "Unit Price", "Subtotal");
            System.out.println("  ------------------------------------------------------------------------");
            for (OrderItem item : order.getItems()) {
                String name = item.getProductName() != null ? item.getProductName() : "SKU-" + item.getSkuId();
                BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() :
                        (item.getQuantity() > 0 ? item.getSubtotal().divide(BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                System.out.printf("  %-6d | %-32s | %-6d | Rs.%-7.2f | Rs.%-7.2f%n",
                        item.getSkuId(),
                        truncate(name, 32),
                        item.getQuantity(),
                        price,
                        item.getSubtotal());
            }
            System.out.println("  ------------------------------------------------------------------------");
            System.out.printf("  GRAND TOTAL: Rs. %.2f%n", order.getTotalAmount());
        }
        System.out.println("==========================================================================");
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
