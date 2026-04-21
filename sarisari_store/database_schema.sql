-- =====================================================
-- SARI-SARI STORE INVENTORY AND SALES MANAGEMENT SYSTEM
-- MySQL/MariaDB Database Schema (Simplified Version)
-- No User Table - Hardcoded Authentication
-- =====================================================

-- Create database
CREATE DATABASE IF NOT EXISTS sarisari_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sarisari_db;

-- =====================================================
-- 1. PRODUCTS TABLE
-- Stores product information with stock tracking
-- Note: category is included as a simple field (not a separate entity)
-- =====================================================
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(150) NOT NULL,
    category VARCHAR(50) DEFAULT 'General',
    unit VARCHAR(20) DEFAULT 'piece',
    cost_per_unit DECIMAL(10,2) NOT NULL DEFAULT 0.00,      -- How much you pay to buy 1 item from supplier
    sell_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,         -- How much you charge customers for 1 item
    current_stock INT DEFAULT 0,
    min_stock_level INT DEFAULT 5,
    expiry_date DATE NULL,
    image_path VARCHAR(255) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_cost CHECK (cost_per_unit >= 0),
    CONSTRAINT chk_sell_price CHECK (sell_price >= cost_per_unit),
    CONSTRAINT chk_stock CHECK (current_stock >= 0),
    CONSTRAINT chk_min_stock CHECK (min_stock_level >= 0)
);

-- Insert sample products
INSERT INTO products (product_name, category, unit, cost_per_unit, sell_price, current_stock, min_stock_level) VALUES
('Coca-Cola 1L', 'Beverages', 'piece', 18.00, 25.00, 50, 10),
('Coca-Cola 500ml', 'Beverages', 'piece', 12.00, 18.00, 30, 10),
('Fudgee Barr Chocolate', 'Snacks', 'piece', 8.00, 12.00, 100, 20),
('Lucky Me Pancit Canton', 'Snacks', 'piece', 15.00, 22.00, 75, 15),
('Safeguard Soap 90g', 'Household', 'piece', 18.00, 28.00, 40, 10),
('Colgate Toothpaste 50ml', 'Household', 'piece', 25.00, 38.00, 25, 5),
('Nescafe 3in1 Original', 'Beverages', 'piece', 5.00, 8.00, 200, 50),
('Gardenia Loaf Bread', 'Food', 'piece', 45.00, 60.00, 15, 5);

-- =====================================================
-- 2. TRANSACTIONS TABLE (formerly sales)
-- Stores each sale transaction (header)
-- No user reference - simplified system
-- =====================================================
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    final_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes VARCHAR(255) NULL
);

-- Indexes for performance
CREATE INDEX idx_transaction_datetime ON transactions(transaction_datetime);

-- =====================================================
-- 3. TRANSACTION_ITEMS TABLE (formerly sale_items)
-- Stores individual line items for each transaction
-- Includes cost snapshot for profit calculation
-- =====================================================
CREATE TABLE transaction_items (
    transaction_item_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    sold_price DECIMAL(10,2) NOT NULL,        -- Actual price customer paid for this item
    cost_at_sale DECIMAL(10,2) NOT NULL,      -- Historical cost when this item was sold (for profit calc)
    item_total DECIMAL(10,2) NOT NULL,        -- Subtotal: sold_price × quantity
    
    -- Foreign Keys
    CONSTRAINT fk_transactionitems_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    CONSTRAINT fk_transactionitems_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    
    -- Constraints
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT chk_sold_price CHECK (sold_price >= 0),
    CONSTRAINT chk_cost_at_sale CHECK (cost_at_sale >= 0),
    CONSTRAINT chk_item_total CHECK (item_total >= 0)
);

CREATE INDEX idx_transactionitems_transaction ON transaction_items(transaction_id);
CREATE INDEX idx_transactionitems_product ON transaction_items(product_id);

-- =====================================================
-- 4. RESTOCK_LOG TABLE
-- Tracks all stock additions and their capital cost
-- Connected to products table (no user reference)
-- =====================================================
CREATE TABLE restock_log (
    restock_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    quantity_added INT NOT NULL,
    cost_per_unit DECIMAL(10,2) NOT NULL,     -- Cost per unit at time of restock
    total_cost DECIMAL(10,2) NOT NULL,        -- Total: cost_per_unit × quantity_added
    restock_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255) NULL,
    
    CONSTRAINT fk_restocklog_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE INDEX idx_restocklog_product ON restock_log(product_id);
CREATE INDEX idx_restocklog_date ON restock_log(restock_date);

-- =====================================================
-- VIEWS FOR COMMON QUERIES
-- =====================================================

-- View: Low stock products
CREATE VIEW low_stock_view AS
SELECT 
    product_id,
    product_name,
    category,
    current_stock,
    min_stock_level,
    (current_stock - min_stock_level) AS stock_deficit
FROM products
WHERE current_stock <= min_stock_level AND is_active = TRUE
ORDER BY stock_deficit ASC;

-- View: Transaction summary (simplified, no user info)
CREATE VIEW transaction_summary_view AS
SELECT 
    t.transaction_id,
    t.transaction_datetime,
    t.total_amount,
    t.discount_amount,
    t.final_amount,
    COUNT(ti.transaction_item_id) AS item_count
FROM transactions t
LEFT JOIN transaction_items ti ON t.transaction_id = ti.transaction_id
GROUP BY t.transaction_id, t.transaction_datetime, t.total_amount, t.discount_amount, t.final_amount;

-- =====================================================
-- End of Schema
-- =====================================================
