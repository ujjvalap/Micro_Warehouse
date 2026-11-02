-- ====================================================================
-- SPRING MINI HACKATHON 2026: Micro-Warehouse Inventory & Fulfillment Hub
-- Seed Data Script (data.sql)
-- Realistic Grocery & Household SKUs for a 10-Minute Dark Store
-- ====================================================================

-- 1. Seed Products Catalog
INSERT INTO products (sku_id, name, category, unit_price, stock_qty) VALUES
(1, 'Amul Taaza Fresh Toned Milk 1L', 'Dairy', 54.00, 50),
(2, 'Nandini Pure Cow Curd 500g', 'Dairy', 32.00, 40),
(3, 'Country Delight Farm Butter 200g', 'Dairy', 95.00, 25),
(4, 'Britannia Cheese Slices 200g', 'Dairy', 140.00, 30),
(5, 'Fresh Cavendish Bananas 500g', 'Produce', 35.00, 60),
(6, 'Hybrid Red Tomatoes 1kg', 'Produce', 40.00, 45),
(7, 'Fresh Green Coriander Bunch 100g', 'Produce', 15.00, 8),
(8, 'Crisp Red Onions 1kg', 'Produce', 45.00, 35),
(9, 'Lays Magic Masala Potato Chips 50g', 'Snacks', 20.00, 100),
(10, 'Haldirams Nagpur Bhujia Sev 200g', 'Snacks', 65.00, 50),
(11, 'Oreo Vanilla Creme Biscuits 120g', 'Snacks', 35.00, 40),
(12, 'Coca-Cola Zero Sugar Can 300ml', 'Beverages', 40.00, 80),
(13, 'Tropicana 100% Orange Juice 1L', 'Beverages', 125.00, 20),
(14, 'Red Bull Energy Drink 250ml', 'Beverages', 125.00, 15),
(15, 'Vim Dishwash Liquid Gel 500ml', 'Household', 105.00, 30),
(16, 'Surf Excel Matic Liquid Detergent 1L', 'Household', 240.00, 20),
(17, 'Dettol Original Liquid Handwash 750ml', 'Household', 119.00, 12);

-- 2. Seed Initial Orders (History)
INSERT INTO orders (order_id, customer_phone, order_status, created_at) VALUES
(1001, '9876543210', 'CONFIRMED', NOW() - INTERVAL 45 MINUTE),
(1002, '9123456780', 'DISPATCHED', NOW() - INTERVAL 25 MINUTE),
(1003, '9988776655', 'REJECTED', NOW() - INTERVAL 10 MINUTE);

-- 3. Seed Initial Order Items
-- Order 1001: 2 x Milk (54.00), 1 x Butter (95.00) => 108.00 + 95.00 = 203.00
INSERT INTO order_items (item_id, order_id, sku_id, quantity, subtotal) VALUES
(1, 1001, 1, 2, 108.00),
(2, 1001, 3, 1, 95.00);

-- Order 1002: 1 x Tomatoes (40.00), 2 x Lays (20.00), 2 x Coke (40.00) => 40.00 + 40.00 + 80.00 = 160.00
INSERT INTO order_items (item_id, order_id, sku_id, quantity, subtotal) VALUES
(3, 1002, 6, 1, 40.00),
(4, 1002, 9, 2, 40.00),
(5, 1002, 12, 2, 80.00);

-- Order 1003: Attempted order with 0 items persisted because of atomic rejection
