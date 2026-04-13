-- =====================================================
-- SARI-SARI STORE INVENTORY AND SALES MANAGEMENT SYSTEM
-- MySQL/MariaDB Database Schema
-- =====================================================

-- Create database
CREATE DATABASE IF NOT EXISTS sarisari_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sarisari_db;

-- =====================================================
-- 1. USERS TABLE
-- Stores system users with role-based access
-- =====================================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('admin', 'cashier') DEFAULT 'cashier' NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default admin user (password: admin123)
-- In production, use proper hashing. For first-year level, plain text or simple hash is acceptable
INSERT INTO users (username, password_hash, full_name, role, is_active) VALUES
('admin', 'admin123', 'System Administrator', 'admin', TRUE),
('cashier1', 'cashier123', 'Juan Dela Cruz', 'cashier', TRUE);

-- =====================================================
-- 2. PRODUCTS TABLE
-- Stores product information with stock tracking
-- =====================================================
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(150) NOT NULL,
    category VARCHAR(50) DEFAULT 'General',
    unit VARCHAR(20) DEFAULT 'piece',
    purchase_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    srp DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    current_stock INT DEFAULT 0,
    min_stock_level INT DEFAULT 5,
    expiry_date DATE NULL,
    image_path VARCHAR(255) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_purchase_price CHECK (purchase_price >= 0),
    CONSTRAINT chk_srp CHECK (srp >= purchase_price),
    CONSTRAINT chk_stock CHECK (current_stock >= 0),
    CONSTRAINT chk_min_stock CHECK (min_stock_level >= 0)
);

-- Insert sample products
INSERT INTO products (product_code, product_name, category, unit, purchase_price, srp, current_stock, min_stock_level) VALUES
('CC-001', 'Coca-Cola 1L', 'Beverages', 'piece', 18.00, 25.00, 50, 10),
('CC-002', 'Coca-Cola 500ml', 'Beverages', 'piece', 12.00, 18.00, 30, 10),
('SN-001', 'Fudgee Barr Chocolate', 'Snacks', 'piece', 8.00, 12.00, 100, 20),
('SN-002', 'Lucky Me Pancit Canton', 'Snacks', 'piece', 15.00, 22.00, 75, 15),
('HH-001', 'Safeguard Soap 90g', 'Household', 'piece', 18.00, 28.00, 40, 10),
('HH-002', 'Colgate Toothpaste 50ml', 'Household', 'piece', 25.00, 38.00, 25, 5),
('NG-001', 'Nescafe 3in1 Original', 'Beverages', 'piece', 5.00, 8.00, 200, 50),
('NG-002', 'Gardenia Loaf Bread', 'Food', 'piece', 45.00, 60.00, 15, 5);

-- =====================================================
-- 3. SALES TABLE (Transaction Header)
-- Stores each sale transaction
-- =====================================================
CREATE TABLE sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    sale_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    final_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes VARCHAR(255) NULL,
    
    -- Foreign Key
    CONSTRAINT fk_sales_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT
);

-- Indexes for performance
CREATE INDEX idx_sale_datetime ON sales(sale_datetime);
CREATE INDEX idx_sale_user ON sales(user_id);

-- =====================================================
-- 4. SALE_ITEMS TABLE (Transaction Lines)
-- Stores individual line items for each sale
-- Includes purchase_price snapshot for profit calculation
-- =====================================================
CREATE TABLE sale_items (
    sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,  -- SRP at time of sale
    purchase_price DECIMAL(10,2) NOT NULL,  -- Cost at time of sale (for profit calc)
    line_total DECIMAL(10,2) NOT NULL,
    
    -- Foreign Keys
    CONSTRAINT fk_saleitems_sale FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    CONSTRAINT fk_saleitems_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    
    -- Constraints
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT chk_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_purchase_price_item CHECK (purchase_price >= 0),
    CONSTRAINT chk_line_total CHECK (line_total >= 0)
);

CREATE INDEX idx_saleitems_sale ON sale_items(sale_id);
CREATE INDEX idx_saleitems_product ON sale_items(product_id);

-- =====================================================
-- 5. STOCK_HISTORY TABLE (Optional - for audit trail)
-- Tracks all stock changes for accountability
-- =====================================================
CREATE TABLE stock_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    change_type ENUM('sale', 'restock', 'adjustment', 'initial') NOT NULL,
    quantity_change INT NOT NULL,  -- positive for restock, negative for sale
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    reference_id INT NULL,  -- sale_id if type is 'sale'
    user_id INT NULL,
    notes VARCHAR(255) NULL,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_stockhist_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_stockhist_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_stockhist_product ON stock_history(product_id);
CREATE INDEX idx_stockhist_date ON stock_history(change_date);

-- =====================================================
-- 5.1 RESTOCK_LOG TABLE
-- Tracks all stock additions and their capital cost
-- =====================================================
CREATE TABLE restock_log (
    restock_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    quantity_added INT NOT NULL,
    purchase_price DECIMAL(10,2) NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    user_id INT NOT NULL,
    restock_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255) NULL,
    
    CONSTRAINT fk_restocklog_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_restocklog_user FOREIGN KEY (user_id) REFERENCES users(user_id)
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
    product_code,
    product_name,
    category,
    current_stock,
    min_stock_level,
    (current_stock - min_stock_level) AS stock_deficit
FROM products
WHERE current_stock <= min_stock_level AND is_active = TRUE
ORDER BY stock_deficit ASC;

-- View: Sales with user details
CREATE VIEW sales_summary_view AS
SELECT 
    s.sale_id,
    s.sale_datetime,
    u.full_name AS cashier_name,
    s.total_amount,
    s.discount_amount,
    s.final_amount,
    COUNT(si.sale_item_id) AS item_count
FROM sales s
JOIN users u ON s.user_id = u.user_id
LEFT JOIN sale_items si ON s.sale_id = si.sale_id
GROUP BY s.sale_id, s.sale_datetime, u.full_name, s.total_amount, s.discount_amount, s.final_amount;

-- =====================================================
-- End of Schema
-- =====================================================
