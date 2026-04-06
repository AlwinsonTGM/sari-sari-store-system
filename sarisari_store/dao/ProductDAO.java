package dao;

import model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO - Data Access Object for Product operations
 * Handles database operations for the products table
 */
public class ProductDAO {
    
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }
    
    /**
     * Get product by ID
     * @param productId the product ID
     * @return Product object or null
     */
    public Product getById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting product: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get product by code
     * @param productCode the product code
     * @return Product object or null
     */
    public Product getByCode(String productCode) {
        String sql = "SELECT * FROM products WHERE product_code = ? AND is_active = TRUE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting product by code: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all active products
     * @return List of active products
     */
    public List<Product> getAllActive() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE ORDER BY product_name";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting products: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Search products by name or code
     * @param searchTerm the search term
     * @return List of matching products
     */
    public List<Product> search(String searchTerm) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE AND " +
                     "(product_name LIKE ? OR product_code LIKE ?) ORDER BY product_name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + searchTerm + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Get products with low stock
     * @return List of products where current_stock <= min_stock_level
     */
    public List<Product> getLowStock() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE AND current_stock <= min_stock_level " +
                     "ORDER BY (current_stock - min_stock_level) ASC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting low stock products: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Get count of low stock products
     * @return count of low stock products
     */
    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = TRUE AND current_stock <= min_stock_level";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting low stock count: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Add a new product
     * @param product the Product to add
     * @return true if successful
     */
    public boolean add(Product product) {
        String sql = "INSERT INTO products (product_code, product_name, category, unit, " +
                     "purchase_price, srp, current_stock, min_stock_level, expiry_date, image_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, product.getProductCode());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getCategory());
            stmt.setString(4, product.getUnit());
            stmt.setDouble(5, product.getPurchasePrice());
            stmt.setDouble(6, product.getSrp());
            stmt.setInt(7, product.getCurrentStock());
            stmt.setInt(8, product.getMinStockLevel());
            
            if (product.getExpiryDate() != null) {
                stmt.setDate(9, product.getExpiryDate());
            } else {
                stmt.setNull(9, Types.DATE);
            }
            
            stmt.setString(10, product.getImagePath());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setProductId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update an existing product
     * @param product the Product to update
     * @return true if successful
     */
    public boolean update(Product product) {
        String sql = "UPDATE products SET product_code = ?, product_name = ?, category = ?, " +
                     "unit = ?, purchase_price = ?, srp = ?, min_stock_level = ?, " +
                     "expiry_date = ?, image_path = ? WHERE product_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getProductCode());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getCategory());
            stmt.setString(4, product.getUnit());
            stmt.setDouble(5, product.getPurchasePrice());
            stmt.setDouble(6, product.getSrp());
            stmt.setInt(7, product.getMinStockLevel());
            
            if (product.getExpiryDate() != null) {
                stmt.setDate(8, product.getExpiryDate());
            } else {
                stmt.setNull(8, Types.DATE);
            }
            
            stmt.setString(9, product.getImagePath());
            stmt.setInt(10, product.getProductId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Restock a product: adds quantity, logs the capital cost.
     * purchasePrice and userId are used to record restock_log.
     * If restock_log table doesn't exist yet, the stock update still succeeds.
     */
    public boolean restock(int productId, int quantity, double purchasePrice, int userId) {
        String stockSql = "UPDATE products SET current_stock = current_stock + ? WHERE product_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(stockSql)) {
            
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            boolean stockUpdated = stmt.executeUpdate() > 0;
            
            if (stockUpdated) {
                // Log the capital cost — silent failure if table doesn't exist yet
                try {
                    String logSql = "INSERT INTO restock_log (product_id, quantity_added, purchase_price, total_cost, user_id) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement logStmt = conn.prepareStatement(logSql);
                    logStmt.setInt(1, productId);
                    logStmt.setInt(2, quantity);
                    logStmt.setDouble(3, purchasePrice);
                    logStmt.setDouble(4, quantity * purchasePrice);
                    logStmt.setInt(5, userId);
                    logStmt.executeUpdate();
                    logStmt.close();
                } catch (SQLException logEx) {
                    System.err.println("[Capital Log] restock_log table may not exist yet: " + logEx.getMessage());
                }
            }
            
            return stockUpdated;
        } catch (SQLException e) {
            System.err.println("Error restocking product: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get total capital spent on restocking today
     */
    public double getTodayRestockCost() {
        String sql = "SELECT COALESCE(SUM(total_cost), 0) FROM restock_log WHERE DATE(restock_date) = CURDATE()";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            // Table may not exist yet
        }
        return 0;
    }
    
    /**
     * Get all-time total capital spent on restocking
     */
    public double getAllTimeRestockCost() {
        String sql = "SELECT COALESCE(SUM(total_cost), 0) FROM restock_log";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            // Table may not exist yet
        }
        return 0;
    }
    
    /**
     * Deduct stock (for sales)
     * @param productId the product ID
     * @param quantity the quantity to deduct
     * @return true if successful and stock doesn't go negative
     */
    public boolean deductStock(int productId, int quantity) {
        String sql = "UPDATE products SET current_stock = current_stock - ? " +
                     "WHERE product_id = ? AND current_stock >= ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);  // Ensure sufficient stock
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deducting stock: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Soft delete (deactivate) a product
     * @param productId the product ID
     * @return true if successful
     */
    public boolean deactivate(int productId) {
        String sql = "UPDATE products SET is_active = FALSE WHERE product_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deactivating product: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if product code already exists
     * @param productCode the code to check
     * @return true if exists
     */
    public boolean codeExists(String productCode) {
        String sql = "SELECT COUNT(*) FROM products WHERE product_code = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking product code: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Product object
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductCode(rs.getString("product_code"));
        product.setProductName(rs.getString("product_name"));
        product.setCategory(rs.getString("category"));
        product.setUnit(rs.getString("unit"));
        product.setPurchasePrice(rs.getDouble("purchase_price"));
        product.setSrp(rs.getDouble("srp"));
        product.setCurrentStock(rs.getInt("current_stock"));
        product.setMinStockLevel(rs.getInt("min_stock_level"));
        product.setExpiryDate(rs.getDate("expiry_date"));
        product.setImagePath(rs.getString("image_path"));
        product.setActive(rs.getBoolean("is_active"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setUpdatedAt(rs.getTimestamp("updated_at"));
        return product;
    }
}
