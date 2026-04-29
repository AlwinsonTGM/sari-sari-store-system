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
     * Search products by name only (product_code removed)
     * @param searchTerm the search term
     * @return List of matching products
     */
    public List<Product> search(String searchTerm) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE AND " +
                     "product_name LIKE ? ORDER BY product_name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + searchTerm + "%";
            stmt.setString(1, pattern);
            
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
     * Add a new product (product_code removed)
     * @param product the Product to add
     * @return true if successful
     */
    public boolean add(Product product) {
        String sql = "INSERT INTO products (product_name, category, unit, " +
                     "cost_per_unit, sell_price, current_stock, min_stock_level, expiry_date, image_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getCategory());
            stmt.setString(3, product.getUnit());
            stmt.setDouble(4, product.getCostPerUnit());
            stmt.setDouble(5, product.getSellPrice());
            stmt.setInt(6, product.getCurrentStock());
            stmt.setInt(7, product.getMinStockLevel());
            
            if (product.getExpiryDate() != null) {
                stmt.setDate(8, product.getExpiryDate());
            } else {
                stmt.setNull(8, Types.DATE);
            }
            
            stmt.setString(9, product.getImagePath());
            
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
     * Update an existing product (product_code removed)
     * @param product the Product to update
     * @return true if successful
     */
    public boolean update(Product product) {
        String sql = "UPDATE products SET product_name = ?, category = ?, " +
                     "unit = ?, cost_per_unit = ?, sell_price = ?, min_stock_level = ?, " +
                     "expiry_date = ?, image_path = ? WHERE product_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getCategory());
            stmt.setString(3, product.getUnit());
            stmt.setDouble(4, product.getCostPerUnit());
            stmt.setDouble(5, product.getSellPrice());
            stmt.setInt(6, product.getMinStockLevel());
            
            if (product.getExpiryDate() != null) {
                stmt.setDate(7, product.getExpiryDate());
            } else {
                stmt.setNull(7, Types.DATE);
            }
            
            stmt.setString(8, product.getImagePath());
            stmt.setInt(9, product.getProductId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Restock a product: adds quantity, logs the capital cost.
     * Removed userId - simplified system has no user table
     */
    public boolean restock(int productId, int quantity, double costPerUnit) {
        String stockSql = "UPDATE products SET current_stock = current_stock + ? WHERE product_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(stockSql)) {
            
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            boolean stockUpdated = stmt.executeUpdate() > 0;
            
            if (stockUpdated) {
                // Log the capital cost — silent failure if table doesn't exist yet
                try {
                    String logSql = "INSERT INTO restock_log (product_id, quantity_added, cost_per_unit, total_cost) VALUES (?, ?, ?, ?)";
                    PreparedStatement logStmt = conn.prepareStatement(logSql);
                    logStmt.setInt(1, productId);
                    logStmt.setInt(2, quantity);
                    logStmt.setDouble(3, costPerUnit);
                    logStmt.setDouble(4, quantity * costPerUnit);
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
     * Get all restock logs (no user reference - simplified system)
     * @return List of RestockLog objects
     */
    public List<model.RestockLog> getRestockLogs() {
        List<model.RestockLog> logs = new ArrayList<>();
        String sql = "SELECT r.*, p.product_name " +
                     "FROM restock_log r " +
                     "JOIN products p ON r.product_id = p.product_id " +
                     "ORDER BY r.restock_date DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                model.RestockLog log = new model.RestockLog();
                log.setLogId(rs.getInt("restock_id"));
                log.setProductId(rs.getInt("product_id"));
                log.setProductName(rs.getString("product_name"));
                log.setQuantityAdded(rs.getInt("quantity_added"));
                log.setCostPerUnit(rs.getDouble("cost_per_unit"));
                log.setTotalCost(rs.getDouble("total_cost"));
                log.setRestockDate(rs.getTimestamp("restock_date"));
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Error getting restock logs: " + e.getMessage());
            // It could be that the restock_log table doesn't exist yet! We log it.
        }
        
        return logs;
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
     * Map ResultSet to Product object
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setCategory(rs.getString("category"));
        product.setUnit(rs.getString("unit"));
        product.setCostPerUnit(rs.getDouble("cost_per_unit"));
        product.setSellPrice(rs.getDouble("sell_price"));
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
