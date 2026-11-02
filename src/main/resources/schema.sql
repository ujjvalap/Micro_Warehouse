-- ====================================================================
-- SPRING MINI HACKATHON 2026: Micro-Warehouse Inventory & Fulfillment Hub
-- Database Schema Script (schema.sql)
-- ====================================================================

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;

-- 1. Products Table
CREATE TABLE products (
    sku_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    stock_qty INT NOT NULL,
    CONSTRAINT chk_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_stock_qty CHECK (stock_qty >= 0)
);

-- 2. Orders Table
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_phone VARCHAR(15) NOT NULL,
    order_status VARCHAR(30) NOT NULL, -- PENDING, CONFIRMED, REJECTED, DISPATCHED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Order Items Table
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

-- Indexes for performance on high-velocity order queries
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_phone ON orders(customer_phone);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_sku_id ON order_items(sku_id);
