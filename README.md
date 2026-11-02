# ⚡ Quick-Commerce Micro-Warehouse: Order Management & Fulfillment Hub

**Spring Mini Hackathon 2026 — Problem Statement #3**  
**Module**: **Order Management (FR-2 & FR-3)**  
**Technology Stack**: Java 17, Spring Boot 3.2.5, Spring JdbcTemplate, MySQL 8.x, Maven  

---

## 📌 Executive Summary & Architectural Overview

In a 10-minute quick-commerce delivery dark store (e.g., Zepto, Blinkit, Instamart), orders are placed and packed in high-velocity bursts. Microsecond inventory discrepancies lead to unfulfillable orders and customer churn.

This module delivers the **Order Management & Fulfillment Hub**, engineered with strict transactional consistency and optimal database performance:
1. **Multi-Item Batch Order Persistence (FR-2)**: Leverages Spring's `JdbcTemplate.batchUpdate()` with `BatchPreparedStatementSetter` to persist all line items for an order in a **single database round-trip**.
2. **Atomic Inventory Verification & Deduction (FR-3)**: Pre-verifies available stock for every SKU in an order within a `@Transactional(rollbackFor = Exception.class)` boundary. If all items have sufficient stock, inventory is atomically deducted and the order is marked `CONFIRMED`. If any single SKU has insufficient stock, the order is marked `REJECTED` and **zero inventory is deducted** from warehouse stock.
3. **Fulfillment & Dispatch Workflow**: Allows warehouse operators to inspect order invoices and dispatch confirmed orders (`CONFIRMED` ➔ `DISPATCHED`) for 10-minute delivery rider pickup.
4. **Interactive Console CLI**: User-friendly console interface providing real-time catalog browsing, multi-item order placement, order lookup, dispatching, and an automated atomic stress test demo.

---

## 🏛️ Tiered Architecture & Spring Beans

The codebase strictly follows clean tiered architectural separation:

```
src/main/java/org/warehouse/
├── Main.java                          # Spring Boot Application & CommandLineRunner
├── config/
│   └── DatabaseConfig.java            # @Configuration: DataSource, JdbcTemplate, PlatformTransactionManager
├── model/
│   ├── Order.java                     # Order header entity with grand total calculation
│   ├── OrderItem.java                 # Line item entity (item_id, order_id, sku_id, quantity, subtotal)
│   ├── OrderStatus.java               # Enum: PENDING, CONFIRMED, REJECTED, DISPATCHED
│   └── Product.java                   # Product SKU entity (sku_id, name, category, unit_price, stock_qty)
├── dto/
│   ├── CreateOrderRequest.java        # Multi-item order request DTO
│   ├── OrderItemRequest.java          # Individual line item request DTO (sku_id, quantity)
│   └── OrderResult.java               # Order processing outcome, status, timing, and rejection reasons
├── dao/
│   ├── OrderDao.java                  # DAO Interface for order headers
│   ├── OrderDaoImpl.java              # @Repository: KeyHolder, custom OrderRowMapper, parameterized SQL
│   ├── OrderItemDao.java              # DAO Interface for line items
│   ├── OrderItemDaoImpl.java          # @Repository: JdbcTemplate.batchUpdate(), custom OrderItemRowMapper
│   ├── ProductDao.java                # DAO Interface for inventory checking & atomic deduction
│   └── ProductDaoImpl.java            # @Repository: custom ProductRowMapper, atomic conditional UPDATEs
├── service/
│   ├── OrderService.java              # Business service interface
│   └── OrderServiceImpl.java          # @Service: @Transactional atomic stock check, batch persistence
└── cli/
    └── OrderManagementCLI.java        # Interactive Console CLI for dark store order fulfillment
```

---

## 💾 Database Schema (`schema.sql` & `data.sql`)

Relational schema with foreign keys, cascading rules, check constraints, and performance indexes:

### `products`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `sku_id` | INT | PRIMARY KEY AUTO_INCREMENT | Unique SKU identifier |
| `name` | VARCHAR(150) | NOT NULL | Product name |
| `category` | VARCHAR(50) | NOT NULL | Dairy, Produce, Snacks, Beverages, Household |
| `unit_price`| DECIMAL(10,2) | NOT NULL, CHECK(>= 0) | Retail price |
| `stock_qty` | INT | NOT NULL, CHECK(>= 0) | Available stock count |

### `orders`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `order_id` | INT | PRIMARY KEY AUTO_INCREMENT | Unique order number |
| `customer_phone` | VARCHAR(15) | NOT NULL | Customer contact phone |
| `order_status` | VARCHAR(30) | NOT NULL | PENDING, CONFIRMED, REJECTED, DISPATCHED |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Order creation timestamp |

### `order_items`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `item_id` | INT | PRIMARY KEY AUTO_INCREMENT | Line item ID |
| `order_id` | INT | FK -> orders(order_id) | Parent order |
| `sku_id` | INT | FK -> products(sku_id) | Referenced product |
| `quantity` | INT | NOT NULL, CHECK(> 0) | Ordered quantity |
| `subtotal` | DECIMAL(10,2)| NOT NULL, CHECK(>= 0) | `quantity * unit_price` |

---

## ⚙️ How It Implements the Core Requirements

### 1. FR-2: Multi-Item Batch Order Persistence via `JdbcTemplate.batchUpdate()`
Line items are inserted efficiently using Spring's batch API in `OrderItemDaoImpl.java`:
```java
String sql = "INSERT INTO order_items (order_id, sku_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        OrderItem item = items.get(i);
        ps.setInt(1, orderId);
        ps.setInt(2, item.getSkuId());
        ps.setInt(3, item.getQuantity());
        ps.setBigDecimal(4, item.getSubtotal());
    }
    @Override
    public int getBatchSize() {
        return items.size();
    }
});
```

### 2. FR-3: Atomic Inventory Deduction with Zero Stock Corruption
Within `OrderServiceImpl.createAndProcessOrder()`:
1. **Pre-check**: For each requested SKU, verify that the SKU exists and `stock_qty >= requested_quantity`.
2. **If any SKU is insufficient**:
   - The entire order is saved as `REJECTED`.
   - **Zero stock is deducted** for any item (even items that had sufficient stock).
   - Descriptive rejection reasons are logged and returned to the caller.
3. **If all SKUs have sufficient stock**:
   - Under `@Transactional`, each SKU's inventory is deducted using parameterized conditional SQL:
     ```sql
     UPDATE products SET stock_qty = stock_qty - ? WHERE sku_id = ? AND stock_qty >= ?
     ```
   - Order status is saved as `CONFIRMED`.
   - All line items are persisted via single-trip `batchUpdate()`.

---

## 🚀 Running the Application & Interactive Demo

### Prerequisites
- Java 17+
- Apache Maven
- MySQL 8.x running on `localhost:3306` with database `micro_warehouse` (configured in `application.properties`)

### Run Interactive Console CLI
```bash
mvn spring-boot:run
```
Once launched, you will see the interactive dark store menu:
```
============================================================================
   ⚡ QUICK-COMMERCE 10-MIN DARK STORE: MICRO-WAREHOUSE ORDER HUB ⚡
   [Spring JdbcTemplate • Atomic Inventory Deduction • Batch Processing]   
============================================================================

----------------- 📦 ORDER MANAGEMENT MENU -----------------
 [1] 📋 View Live Product Catalog & Stock Levels
 [2] 🛒 Place Multi-Item Order (FR-2 Batch Insert + FR-3 Atomic Stock)
 [3] 🔍 Look Up Order by Order ID (Itemized Invoice)
 [4] 🚚 Dispatch Order for 10-Min Delivery (CONFIRMED -> DISPATCHED)
 [5] 📜 View All Warehouse Orders (Filter by Status)
 [6] 📱 Search Order History by Customer Phone
 [7] 🧪 Run Atomic Rollback / Consistency Stress Demo
 [0] 🚪 Exit Application
------------------------------------------------------------
```

### Run Automated Test Suite
```bash
mvn test
```
The integration test suite (`OrderServiceIntegrationTest.java`) tests:
- Multi-item batch order creation with accurate stock deduction.
- Atomic order rejection with zero stock deduction when any single SKU has insufficient stock.
- Order dispatch transition from `CONFIRMED` to `DISPATCHED`.

---

## 👥 GitHub Collaboration & Branching Strategy

For team delivery in a 6-member hackathon:
- **Branch**: `feature/order-management`
- **Modules Covered**:
  - `OrderDao` & `OrderItemDao` (`batchUpdate()`, custom `RowMapper`s, `KeyHolder`)
  - `OrderService` (Atomic multi-item order fulfillment, rejection handling, dispatching)
  - `OrderManagementCLI` (Interactive console user interface)
  - `schema.sql` & `data.sql` (Relational tables and seed data)
  - `OrderServiceIntegrationTest` (Automated integration verification)
