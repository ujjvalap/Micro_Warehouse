# ⚡ Quick-Commerce Micro-Warehouse Inventory & Fulfillment Hub
**Spring Mini Hackathon 2026 — Problem Statement #3**  
**Domain**: Quick-Commerce (Q-Commerce) & Supply Chain (10-Minute Dark Store)  
**Architecture**: Layered Spring Boot + Spring JdbcTemplate + MySQL 8.x + Maven (Java 21)

---

## 📋 Table of Contents
1. [Project Overview & Problem Context](#-project-overview--problem-context)
2. [Functional Modules (FR-1 through FR-5)](#-functional-modules-fr-1-through-fr-5)
3. [Unified Folder Structure](#-unified-folder-structure)
4. [Spring Architecture & Beans Separation](#-spring-architecture--beans-separation)
5. [Database Schema & Seed Data (MySQL)](#-database-schema--seed-data-mysql)
6. [Team Collaboration & Module Ownership](#-team-collaboration--module-ownership)
7. [How to Run Locally](#-how-to-run-locally)
8. [Testing & Verification](#-testing--verification)

---

## 🚀 Project Overview & Problem Context
In 10-minute quick-commerce delivery dark stores (e.g., Zepto, Blinkit, Instamart), thousands of stock movements occur every minute. Microsecond inventory discrepancies lead to out-of-stock items, failed order fulfillments, and customer dissatisfaction.

This application is a unified, production-ready solution that combines:
- **Strict Stock Consistency**: Row-level locking (`SELECT ... FOR UPDATE`) and atomic inventory deductions ensure zero stock corruption on partial or failed orders.
- **High-Velocity Batch Operations**: Spring `JdbcTemplate.batchUpdate()` persists multi-item orders in a single database round-trip.
- **Automated Restock Threshold Monitoring**: Automatic alerts for SKUs dropping below critical thresholds (< 10 units) with rapid replenishment.
- **Real-Time Fulfillment & Top-Mover Auditing**: SQL joins across `orders`, `order_items`, and `products` calculate fulfillment revenue and identify top-selling SKUs.
- **Interactive Console CLI**: User-friendly menu connecting all dark store operations.

---

## 🎯 Functional Modules (FR-1 through FR-5)

### FR-1: SKU Catalog & Inventory Replenishment
- Register new product SKUs with name, category, unit price, and stock count.
- Update product details (category, price).
- Replenish stock quantities for existing products.
- Handled by `ProductDao` and `ProductService`.

### FR-2: Multi-Item Batch Order Creation
- Accept orders containing multiple items and quantities.
- Consolidate duplicate SKUs within the same order automatically.
- Persist all line items into the `order_items` table in a **single round-trip** using Spring's `JdbcTemplate.batchUpdate()` and `BatchPreparedStatementSetter`.
- Handled by `OrderItemDao` and `OrderService`.

### FR-3: Atomic Inventory Deduction & Zero Corruption
- Locks rows with `FOR UPDATE` and verifies stock for **every single SKU** in the order before making deductions.
- If **all** items are available: inventory is deducted atomically, order status is set to `CONFIRMED`.
- If **any single** SKU has insufficient stock: order status is set to `REJECTED`, and **zero stock is deducted** from any item.
- Orders can be dispatched (`CONFIRMED` ➔ `DISPATCHED`) for 10-minute delivery rider pickup.
- Handled by `OrderDao`, `ProductDao`, and `OrderService`.

### FR-4: Automated Restock Alerts
- Query products where `stock_qty` falls below a threshold (default `< 10` units).
- Formatted alert view highlights critical SKUs and allows store pickers to trigger instant replenishment.
- Handled by `ProductDao` and `RestockService`.

### FR-5: Sales Revenue & Top-Mover Audit
- Calculates total fulfillment revenue from `CONFIRMED` and `DISPATCHED` orders.
- Identifies the top fast-moving SKUs by units sold and revenue generated.
- Provides category-wise and daily sales breakdowns.
- Handled by `SalesDao` and `SalesService`.

---

## 📂 Unified Folder Structure

```
Micro_Warehouse/
├── pom.xml                                      # Maven project with Spring Boot 3.3.5 & Java 21
├── README.md                                    # Comprehensive documentation
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── warehouse/
│   │   │           ├── MicroWarehouseApplication.java   # Spring Boot Main & CommandLineRunner
│   │   │           ├── cli/
│   │   │           │   └── WarehouseConsoleCLI.java     # Interactive multi-module Console CLI
│   │   │           ├── config/
│   │   │           │   └── DatabaseConfig.java          # @Configuration: JdbcTemplate & TxManager
│   │   │           ├── dao/
│   │   │           │   ├── OrderDao.java                # Orders DAO interface
│   │   │           │   ├── OrderItemDao.java            # Order items DAO interface
│   │   │           │   ├── ProductDao.java              # Product & inventory DAO interface
│   │   │           │   ├── SalesDao.java                # Sales & audit DAO interface
│   │   │           │   └── impl/
│   │   │           │       ├── OrderDaoImpl.java        # KeyHolder + custom RowMapper
│   │   │           │       ├── OrderItemDaoImpl.java    # batchUpdate() + RowMapper
│   │   │           │       ├── ProductDaoImpl.java      # Row-locking, batch deduction, parameterized SQL
│   │   │           │       └── SalesDaoImpl.java        # Multi-table joins for revenue & top movers
│   │   │           ├── dto/
│   │   │           │   ├── CreateOrderRequest.java      # Order placement payload
│   │   │           │   ├── OrderItemRequest.java        # Line item payload (sku_id, quantity)
│   │   │           │   └── OrderResult.java             # Order fulfillment result DTO
│   │   │           ├── model/
│   │   │           │   ├── Order.java                   # Order header entity
│   │   │           │   ├── OrderItem.java               # Order line item entity
│   │   │           │   ├── OrderStatus.java             # Enum: PENDING, CONFIRMED, REJECTED, DISPATCHED
│   │   │           │   ├── Product.java                 # Product SKU entity
│   │   │           │   └── TopMoverReport.java          # Top-mover audit projection
│   │   │           └── service/
│   │   │               ├── OrderService.java            # Multi-item batch fulfillment service interface
│   │   │               ├── ProductService.java          # Catalog management service interface
│   │   │               ├── RestockService.java          # Restock alerts service interface
│   │   │               ├── SalesService.java            # Revenue & top-mover service interface
│   │   │               └── impl/
│   │   │                   ├── OrderServiceImpl.java   # @Transactional atomic stock validation & batch insert
│   │   │                   ├── ProductServiceImpl.java # Catalog and replenishment logic
│   │   │                   ├── RestockServiceImpl.java # Low stock queries & quick restock
│   │   │                   └── SalesServiceImpl.java   # Financial aggregations and top-mover ranking
│   │   └── resources/
│   │       ├── application.properties                   # MySQL connection, HikariCP, and init configs
│   │       ├── data.sql                                 # Realistic grocery & dark-store seed data
│   │       └── schema.sql                               # DDL schema with constraints & indexes
│   └── test/
│       └── java/
│           └── com/
│               └── warehouse/
│                   ├── MicroWarehouseApplicationTests.java  # Spring Boot Context load test
│                   ├── OrderServiceIntegrationTest.java     # FR-2 & FR-3 integration tests (atomic consistency)
│                   ├── dao/
│                   │   └── impl/
│                   │       └── ProductDaoImplTest.java      # DAO unit tests with Mockito
│                   └── service/
│                       └── impl/
│                           ├── ProductServiceImplTest.java  # Catalog service unit tests
│                           ├── RestockServiceImplTest.java  # Restock service unit tests
│                           └── SalesServiceImplTest.java    # Sales audit service unit tests
```

---

## 🏛️ Spring Architecture & Beans Separation

| Layer | Annotation | Responsibility |
|---|---|---|
| **Configuration** | `@Configuration` | `DatabaseConfig`: Defines `JdbcTemplate` with fetch size tuning and `PlatformTransactionManager` (`DataSourceTransactionManager`). |
| **DAO / Repository** | `@Repository` | `ProductDaoImpl`, `OrderDaoImpl`, `OrderItemDaoImpl`, `SalesDaoImpl`: Parameterized SQL, custom `RowMapper` lambdas, `JdbcTemplate.batchUpdate()`, and safe `DataAccessException` handling. |
| **Service Tier** | `@Service` | `OrderServiceImpl`, `ProductServiceImpl`, `RestockServiceImpl`, `SalesServiceImpl`: Business rules, validation, and `@Transactional(rollbackFor = Exception.class)` boundary management. |
| **User Interaction** | `@Component` | `WarehouseConsoleCLI`: Formatted menus, user input validation, colored status indicators, and stress-test demo. |

---

## 💾 Database Schema & Seed Data (MySQL)

### Table: `products`
```sql
CREATE TABLE products (
    sku_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    stock_qty INT NOT NULL,
    CONSTRAINT chk_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_stock_qty CHECK (stock_qty >= 0)
);
```

### Table: `orders`
```sql
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_phone VARCHAR(15) NOT NULL,
    order_status VARCHAR(30) NOT NULL, -- PENDING, CONFIRMED, REJECTED, DISPATCHED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Table: `order_items`
```sql
CREATE TABLE order_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    sku_id INT NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_products FOREIGN KEY (sku_id) REFERENCES products(sku_id),
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT chk_subtotal CHECK (subtotal >= 0)
);
```

---

## 👥 Team Collaboration & Module Ownership

| Member | Module / Focus Area | Key Contributions |
|---|---|---|
| **Member 1** | **FR-1**: SKU Catalog & Replenishment | `ProductDao`, `ProductService`, SKU registration, stock replenishment. |
| **Member 2** | **FR-2**: Multi-Item Batch Persistence | `OrderItemDao`, `JdbcTemplate.batchUpdate()`, `BatchPreparedStatementSetter`. |
| **Member 3** | **FR-3**: Atomic Inventory Deduction | `OrderDao`, `OrderService`, row locking (`FOR UPDATE`), atomic rejection without stock corruption. |
| **Member 4** | **FR-4**: Automated Restock Alerts | `RestockService`, low-stock queries (< 10 units), picker alert notifications. |
| **Member 5** | **FR-5**: Sales Revenue & Top Movers | `SalesDao`, `SalesService`, multi-table SQL joins, top-mover ranking. |
| **Member 6** | **Architecture & Console CLI** | `DatabaseConfig`, `WarehouseConsoleCLI`, `MicroWarehouseApplication`, schema & integration. |

---

## ⚙️ How to Run Locally

### Prerequisites
- **Java**: JDK 21 (or 17)
- **Maven**: 3.8+
- **MySQL Server**: 8.x running on `localhost:3306` with username `root` and password `root` (or update `src/main/resources/application.properties`).

### 1. Build and Run Tests
```bash
mvn clean test
```

### 2. Launch the Interactive Application
```bash
mvn spring-boot:run
```

---

## 🧪 Testing & Verification

The project includes 22 automated unit and integration tests:
- **`OrderServiceIntegrationTest`**:
  - Tests multi-item order placement with `JdbcTemplate.batchUpdate()`
  - Verifies atomic rejection: attempts an order with 1 valid item and 1 over-requested item, verifying that **zero stock is deducted** from the valid item.
  - Tests order dispatching (`CONFIRMED` ➔ `DISPATCHED`).
- **`ProductDaoImplTest` & `ProductServiceImplTest`**:
  - Tests SKU addition, query, update, and replenishment.
- **`RestockServiceImplTest`**:
  - Tests threshold filtering for low-stock SKUs.
- **`SalesServiceImplTest`**:
  - Tests fulfillment revenue calculation and top-mover ranking.
- **`MicroWarehouseApplicationTests`**:
  - Tests Spring application context loading and HikariCP connection pooling.

```
[INFO] Results:
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
