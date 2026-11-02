package com.warehouse.cli;

import com.warehouse.dto.CreateOrderRequest;
import com.warehouse.dto.OrderItemRequest;
import com.warehouse.dto.OrderResult;
import com.warehouse.model.Order;
import com.warehouse.model.OrderItem;
import com.warehouse.model.OrderStatus;
import com.warehouse.model.Product;
import com.warehouse.model.TopMoverReport;
import com.warehouse.service.OrderService;
import com.warehouse.service.ProductService;
import com.warehouse.service.RestockService;
import com.warehouse.service.SalesService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive Console CLI uniting all 6 team members' functional modules:
 * FR-1: SKU Catalog & Inventory Replenishment
 * FR-2: Multi-Item Batch Order Creation (batchUpdate)
 * FR-3: Atomic Inventory Deduction & Zero Corruption
 * FR-4: Automated Restock Alerts (< 10 units threshold)
 * FR-5: Sales Revenue & Top-Mover Audit
 */
@Component
public class WarehouseConsoleCLI {

    private final ProductService productService;
    private final OrderService orderService;
    private final RestockService restockService;
    private final SalesService salesService;
    private final Scanner scanner = new Scanner(System.in);

    public WarehouseConsoleCLI(ProductService productService,
                               OrderService orderService,
                               RestockService restockService,
                               SalesService salesService) {
        this.productService = productService;
        this.orderService = orderService;
        this.restockService = restockService;
        this.salesService = salesService;
    }

    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("👉 Select an option [0-6]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleProductCatalogMenu();
                    break;
                case "2":
                    handlePlaceMultiItemOrder();
                    break;
                case "3":
                    handleOrderManagementMenu();
                    break;
                case "4":
                    handleRestockAlerts();
                    break;
                case "5":
                    handleSalesRevenueAudit();
                    break;
                case "6":
                    handleRunAtomicIntegrityDemo();
                    break;
                case "0":
                    System.out.println("\n============================================================================");
                    System.out.println("  👋 Exiting Quick-Commerce Micro-Warehouse Hub. Happy Delivering! ⚡");
                    System.out.println("============================================================================\n");
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option. Please enter a choice between 0 and 6.\n");
            }
        }
    }

    private void printBanner() {
        System.out.println("============================================================================");
        System.out.println("   ⚡ QUICK-COMMERCE 10-MIN DARK STORE: MICRO-WAREHOUSE HUB ⚡");
        System.out.println("   Spring Boot JdbcTemplate • Batch Operations • Strict Stock Consistency   ");
        System.out.println("============================================================================");
    }

    private void printMainMenu() {
        System.out.println("\n----------------------- 🏪 MAIN DASHBOARD -----------------------");
        System.out.println(" [1] 📋 Product Catalog & Inventory Replenishment (FR-1)");
        System.out.println(" [2] 🛒 Place Multi-Item Batch Order (FR-2 Batch + FR-3 Atomic)");
        System.out.println(" [3] 🚚 Order Tracking & 10-Min Quick Dispatch (CONFIRMED -> DISPATCHED)");
        System.out.println(" [4] ⚠️ Automated Low-Stock Restock Alerts (FR-4)");
        System.out.println(" [5] 📊 Sales Revenue & Top-Mover Audit (FR-5)");
        System.out.println(" [6] 🧪 Run Atomic Integrity & Zero-Corruption Stress Test");
        System.out.println(" [0] 🚪 Exit Application");
        System.out.println("-----------------------------------------------------------------");
    }

    // =========================================================================
    // FR-1: Catalog & Replenishment
    // =========================================================================
    private void handleProductCatalogMenu() {
        System.out.println("\n--- [FR-1] PRODUCT CATALOG & REPLENISHMENT ---");
        System.out.println(" 1. View All Products");
        System.out.println(" 2. Register New SKU");
        System.out.println(" 3. Replenish Stock for Existing SKU");
        System.out.println(" 4. Update Product Price & Category");
        System.out.print("👉 Choice [1-4]: ");
        String subChoice = scanner.nextLine().trim();

        switch (subChoice) {
            case "1":
                displayProductList();
                break;
            case "2":
                handleRegisterProduct();
                break;
            case "3":
                handleReplenishStock();
                break;
            case "4":
                handleUpdateProduct();
                break;
            default:
                System.out.println("Invalid catalog choice.");
        }
    }

    private void displayProductList() {
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products found in the catalog.");
            return;
        }

        System.out.println("\n================================= CURRENT CATALOG =================================");
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

    private void handleRegisterProduct() {
        System.out.println("\n--- Register New Product SKU ---");
        System.out.print("Product Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Category (Dairy, Produce, Snacks, Beverages, Household): ");
        String category = scanner.nextLine().trim();

        System.out.print("Unit Price (e.g. 45.00): ");
        String priceStr = scanner.nextLine().trim();

        System.out.print("Initial Stock Quantity: ");
        String stockStr = scanner.nextLine().trim();

        try {
            BigDecimal price = new BigDecimal(priceStr);
            int stock = Integer.parseInt(stockStr);

            Product product = new Product(name, category, price, stock);
            productService.addProduct(product);
            System.out.println("✅ Product registered successfully with SKU ID: " + product.getSkuId());
        } catch (Exception e) {
            System.out.println("❌ Failed to register product: " + e.getMessage());
        }
    }

    private void handleReplenishStock() {
        System.out.print("\nEnter SKU ID to replenish: ");
        String skuStr = scanner.nextLine().trim();
        System.out.print("Enter Quantity to add: ");
        String qtyStr = scanner.nextLine().trim();

        try {
            int skuId = Integer.parseInt(skuStr);
            int qty = Integer.parseInt(qtyStr);

            restockService.replenishStock(skuId, qty);
            System.out.println("✅ Stock replenished successfully for SKU " + skuId);
        } catch (Exception e) {
            System.out.println("❌ Failed to replenish stock: " + e.getMessage());
        }
    }

    private void handleUpdateProduct() {
        System.out.print("\nEnter SKU ID to update: ");
        String skuStr = scanner.nextLine().trim();

        try {
            int skuId = Integer.parseInt(skuStr);
            Optional<Product> prodOpt = productService.getProductById(skuId);
            if (prodOpt.isEmpty()) {
                System.out.println("❌ Product not found with SKU ID: " + skuId);
                return;
            }

            Product prod = prodOpt.get();
            System.out.println("Current details: " + prod.getName() + " | " + prod.getCategory() + " | Rs." + prod.getUnitPrice());

            System.out.print("Enter New Name (or Enter to keep current): ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) prod.setName(newName);

            System.out.print("Enter New Category (or Enter to keep current): ");
            String newCat = scanner.nextLine().trim();
            if (!newCat.isEmpty()) prod.setCategory(newCat);

            System.out.print("Enter New Unit Price (or Enter to keep current): ");
            String newPriceStr = scanner.nextLine().trim();
            if (!newPriceStr.isEmpty()) prod.setUnitPrice(new BigDecimal(newPriceStr));

            productService.updateProduct(prod);
            System.out.println("✅ Product updated successfully!");
        } catch (Exception e) {
            System.out.println("❌ Update failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // FR-2 & FR-3: Multi-Item Batch Order Fulfillment
    // =========================================================================
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
                System.out.println("⚠️ Please enter a numerical SKU ID.");
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

        System.out.println("\n⏳ Executing atomic fulfillment via Spring JdbcTemplate.batchUpdate()...");
        CreateOrderRequest request = new CreateOrderRequest(phone, items);
        OrderResult result = orderService.createAndProcessOrder(request);

        printOrderResult(result);
    }

    // =========================================================================
    // Order Management & Dispatch
    // =========================================================================
    private void handleOrderManagementMenu() {
        System.out.println("\n--- 🚚 ORDER TRACKING & DISPATCH ---");
        System.out.println(" 1. View Order Details by Order ID (Invoice)");
        System.out.println(" 2. Dispatch CONFIRMED Order (10-Min Rider Handover)");
        System.out.println(" 3. View All Warehouse Orders");
        System.out.println(" 4. Filter Orders by Status (CONFIRMED / DISPATCHED / REJECTED)");
        System.out.println(" 5. Search Orders by Customer Phone");
        System.out.print("👉 Choice [1-5]: ");
        String subChoice = scanner.nextLine().trim();

        switch (subChoice) {
            case "1":
                handleViewOrderById();
                break;
            case "2":
                handleDispatchOrder();
                break;
            case "3":
                displayOrders(orderService.getAllOrders(), "ALL ORDERS");
                break;
            case "4":
                handleFilterOrdersByStatus();
                break;
            case "5":
                handleSearchOrdersByPhone();
                break;
            default:
                System.out.println("Invalid choice.");
        }
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
            System.out.println("⚠️ Invalid Order ID.");
        }
    }

    private void handleDispatchOrder() {
        System.out.print("\nEnter Order ID to dispatch: ");
        String input = scanner.nextLine().trim();
        try {
            int orderId = Integer.parseInt(input);
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                System.out.println("❌ Order #" + orderId + " not found.");
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
                System.out.println("✅ Order #" + orderId + " DISPATCHED for 10-minute quick-commerce delivery!");
            } else {
                System.out.println("❌ Failed to dispatch Order #" + orderId + ".");
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid Order ID.");
        }
    }

    private void handleFilterOrdersByStatus() {
        System.out.println("Select Status: 1. CONFIRMED  2. DISPATCHED  3. REJECTED");
        System.out.print("👉 Choice: ");
        String sc = scanner.nextLine().trim();
        OrderStatus status = switch (sc) {
            case "2" -> OrderStatus.DISPATCHED;
            case "3" -> OrderStatus.REJECTED;
            default -> OrderStatus.CONFIRMED;
        };

        displayOrders(orderService.getOrdersByStatus(status), status.name() + " ORDERS");
    }

    private void handleSearchOrdersByPhone() {
        System.out.print("Enter Customer Phone: ");
        String phone = scanner.nextLine().trim();
        List<Order> orders = orderService.getOrdersByCustomerPhone(phone);
        displayOrders(orders, "ORDERS FOR " + phone);
    }

    private void displayOrders(List<Order> orders, String title) {
        if (orders.isEmpty()) {
            System.out.println("No orders found matching: " + title);
            return;
        }

        System.out.println("\n============================= " + title + " =============================");
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

    // =========================================================================
    // FR-4: Automated Restock Alerts
    // =========================================================================
    private void handleRestockAlerts() {
        System.out.println("\n====================== ⚠️ AUTOMATED RESTOCK ALERTS ======================");
        System.out.print("Enter Low-Stock Threshold [Default: 10 units]: ");
        String threshInput = scanner.nextLine().trim();
        int threshold = 10;
        if (!threshInput.isEmpty()) {
            try {
                threshold = Integer.parseInt(threshInput);
            } catch (NumberFormatException ignored) {
            }
        }

        List<Product> lowStock = restockService.getLowStockProducts(threshold);
        if (lowStock.isEmpty()) {
            System.out.println("🎉 All SKUs are healthy! No products currently below threshold (" + threshold + " units).");
            return;
        }

        System.out.println("🚨 ATTENTION STORE PICKERS: The following " + lowStock.size() + " SKU(s) require urgent restock:\n");
        System.out.printf("%-6s | %-38s | %-12s | %-10s | %-8s%n",
                "SKU ID", "Product Name", "Category", "Price", "Stock");
        System.out.println("----------------------------------------------------------------------------------");
        for (Product p : lowStock) {
            System.out.printf("%-6d | %-38s | %-12s | Rs.%-7.2f | %-8s%n",
                    p.getSkuId(),
                    truncate(p.getName(), 38),
                    p.getCategory(),
                    p.getUnitPrice(),
                    p.getStockQty() + " (CRITICAL)");
        }
        System.out.println("----------------------------------------------------------------------------------");

        System.out.print("\nWould you like to replenish any of these SKUs right now? (y/n): ");
        String answer = scanner.nextLine().trim();
        if (answer.equalsIgnoreCase("y")) {
            handleReplenishStock();
        }
    }

    // =========================================================================
    // FR-5: Sales Revenue & Top-Mover Audit
    // =========================================================================
    private void handleSalesRevenueAudit() {
        System.out.println("\n======================== 📊 SALES & TOP-MOVER AUDIT ========================");
        BigDecimal totalRevenue = salesService.getTotalRevenue();
        System.out.printf("💰 Total Warehouse Fulfillment Revenue: Rs. %.2f%n", totalRevenue);
        System.out.println("   (Calculated from CONFIRMED and DISPATCHED orders)\n");

        System.out.println("🔥 Top 5 Best-Selling Fast Movers:");
        List<TopMoverReport> topMovers = salesService.getTopSellingProducts(5);
        if (topMovers.isEmpty()) {
            System.out.println("   No sales data recorded yet.");
        } else {
            System.out.printf("   %-6s | %-36s | %-12s | %-10s | %-12s%n",
                    "SKU ID", "Product Name", "Category", "Units Sold", "Revenue (Rs.)");
            System.out.println("   --------------------------------------------------------------------------------");
            for (TopMoverReport r : topMovers) {
                System.out.printf("   %-6d | %-36s | %-12s | %-10d | Rs.%-10.2f%n",
                        r.getSkuId(),
                        truncate(r.getProductName(), 36),
                        r.getCategory(),
                        r.getTotalUnitsSold(),
                        r.getTotalRevenue());
            }
        }

        System.out.println("\n📦 Category Breakdown:");
        List<Map<String, Object>> catReports = salesService.getCategoryRevenueReport();
        if (catReports.isEmpty()) {
            System.out.println("   No category sales data.");
        } else {
            System.out.printf("   %-15s | %-12s | %-12s | %-12s%n",
                    "Category", "Orders Count", "Units Sold", "Revenue (Rs.)");
            System.out.println("   ----------------------------------------------------------");
            for (Map<String, Object> row : catReports) {
                System.out.printf("   %-15s | %-12s | %-12s | Rs.%-10s%n",
                        row.get("category"),
                        row.get("total_orders"),
                        row.get("total_units_sold"),
                        row.get("category_revenue"));
            }
        }
        System.out.println("============================================================================");
    }

    // =========================================================================
    // Stress Test & Demo
    // =========================================================================
    private void handleRunAtomicIntegrityDemo() {
        System.out.println("\n================= 🧪 ATOMIC CONSISTENCY STRESS DEMO =================");
        System.out.println("Scenario: A customer places a multi-item order with 2 items:");
        System.out.println("  - Item 1: SKU 1 (Amul Milk) x 2 units [IN STOCK]");
        System.out.println("  - Item 2: SKU 7 (Fresh Coriander) x 9999 units [MASSIVE SHORTAGE]");
        System.out.println("\nTesting Atomic Rule: If any single SKU is insufficient, the entire order");
        System.out.println("MUST be REJECTED and ZERO inventory deducted from SKU 1 (Amul Milk).");
        System.out.println("----------------------------------------------------------------------");

        Optional<Product> milkPreOpt = productService.getProductById(1);
        int milkPreStock = milkPreOpt.map(Product::getStockQty).orElse(-1);
        System.out.println("📊 Pre-test SKU 1 (Milk) Stock: " + milkPreStock + " units");

        CreateOrderRequest request = new CreateOrderRequest("9998887776", List.of(
                new OrderItemRequest(1, 2),
                new OrderItemRequest(7, 9999)
        ));

        OrderResult result = orderService.createAndProcessOrder(request);
        printOrderResult(result);

        Optional<Product> milkPostOpt = productService.getProductById(1);
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
