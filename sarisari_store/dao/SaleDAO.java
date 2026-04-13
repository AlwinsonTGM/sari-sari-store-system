package dao;

import model.Sale;
import model.SaleItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SaleDAO - Data Access Object for Sale operations
 * Handles database operations for sales and sale_items tables
 */
public class SaleDAO {
    
    private ProductDAO productDAO;
    
    public SaleDAO() {
        this.productDAO = new ProductDAO();
    }
    
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }
    
    /**
     * Create a new sale with all its items (TRANSACTION)
     * This method handles the complete sale process:
     * 1. Insert sale record
     * 2. Insert all sale items
     * 3. Deduct stock from products
     * All operations are in a single transaction
     * 
     * @param sale the Sale to create
     * @return true if successful, false otherwise
     */
    public boolean createSale(Sale sale) {
        Connection conn = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);  // Start transaction
            
            // 1. Insert the sale record
            String saleSql = "INSERT INTO sales (user_id, sale_datetime, total_amount, discount_amount, final_amount, notes) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            saleStmt.setInt(1, sale.getUserId());
            saleStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            saleStmt.setDouble(3, sale.getTotalAmount());
            saleStmt.setDouble(4, sale.getDiscountAmount());
            saleStmt.setDouble(5, sale.getFinalAmount());
            saleStmt.setString(6, sale.getNotes());
            
            int affectedRows = saleStmt.executeUpdate();
            
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }
            
            // Get the generated sale ID
            ResultSet generatedKeys = saleStmt.getGeneratedKeys();
            int saleId;
            if (generatedKeys.next()) {
                saleId = generatedKeys.getInt(1);
                sale.setSaleId(saleId);
            } else {
                conn.rollback();
                return false;
            }
            
            // 2. Insert sale items and update stock
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, purchase_price, line_total) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
            
            String stockSql = "UPDATE products SET current_stock = current_stock - ? " +
                              "WHERE product_id = ? AND current_stock >= ?";
            
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            PreparedStatement stockStmt = conn.prepareStatement(stockSql);
            
            for (SaleItem item : sale.getItems()) {
                // Insert sale item
                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getUnitPrice());
                itemStmt.setDouble(5, item.getPurchasePrice());
                itemStmt.setDouble(6, item.getLineTotal());
                itemStmt.addBatch();
                
                // Update stock
                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.setInt(3, item.getQuantity());  // Ensure sufficient stock
                
                int stockUpdated = stockStmt.executeUpdate();
                if (stockUpdated == 0) {
                    // Insufficient stock - rollback
                    conn.rollback();
                    return false;
                }
            }
            
            // Execute batch insert for items
            itemStmt.executeBatch();
            
            // Commit transaction
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error creating sale: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get sale by ID with all items
     * @param saleId the sale ID
     * @return Sale object with items, or null
     */
    public Sale getById(int saleId) {
        String sql = "SELECT s.*, u.full_name as cashier_name, (SELECT COUNT(*) FROM sale_items si WHERE si.sale_id = s.sale_id) as item_count FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id WHERE s.sale_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Sale sale = mapResultSetToSale(rs);
                sale.setItems(getSaleItems(saleId));
                return sale;
            }
        } catch (SQLException e) {
            System.err.println("Error getting sale: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all sales (for history)
     * @return List of sales
     */
    public List<Sale> getAll() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name as cashier_name, (SELECT COUNT(*) FROM sale_items si WHERE si.sale_id = s.sale_id) as item_count FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id ORDER BY s.sale_datetime DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales: " + e.getMessage());
        }
        
        return sales;
    }
    
    /**
     * Get sales within date range
     * @param fromDate start date (inclusive)
     * @param toDate end date (inclusive)
     * @return List of sales
     */
    public List<Sale> getByDateRange(Date fromDate, Date toDate) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name as cashier_name, (SELECT COUNT(*) FROM sale_items si WHERE si.sale_id = s.sale_id) as item_count FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "WHERE DATE(s.sale_datetime) BETWEEN ? AND ? " +
                     "ORDER BY s.sale_datetime DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, fromDate);
            stmt.setDate(2, toDate);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales by date range: " + e.getMessage());
        }
        
        return sales;
    }
    
    /**
     * Get today's sales
     * @return List of today's sales
     */
    public List<Sale> getTodaySales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name as cashier_name, (SELECT COUNT(*) FROM sale_items si WHERE si.sale_id = s.sale_id) as item_count FROM sales s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "WHERE DATE(s.sale_datetime) = CURDATE() " +
                     "ORDER BY s.sale_datetime DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's sales: " + e.getMessage());
        }
        
        return sales;
    }
    
    /**
     * Get today's total sales amount
     * @return total sales amount for today
     */
    public double getTodayTotalSales() {
        String sql = "SELECT COALESCE(SUM(final_amount), 0) as total FROM sales WHERE DATE(sale_datetime) = CURDATE()";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's total: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    /**
     * Get today's transaction count
     * @return number of transactions today
     */
    public int getTodayTransactionCount() {
        String sql = "SELECT COUNT(*) as count FROM sales WHERE DATE(sale_datetime) = CURDATE()";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's count: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Get today's profit
     * @return total profit for today
     */
    public double getTodayProfit() {
        String sql = "SELECT COALESCE(SUM((si.unit_price - si.purchase_price) * si.quantity), 0) as profit " +
                     "FROM sale_items si JOIN sales s ON si.sale_id = s.sale_id " +
                     "WHERE DATE(s.sale_datetime) = CURDATE()";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("profit");
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's profit: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    /**
     * Get top selling products for today
     * @param limit number of products to return
     * @return List of Object arrays [product_name, total_quantity_sold]
     */
    public List<Object[]> getTopSellingProductsToday(int limit) {
        List<Object[]> topProducts = new ArrayList<>();
        String sql = "SELECT p.product_name, SUM(si.quantity) as total_qty " +
                     "FROM sale_items si " +
                     "JOIN sales s ON si.sale_id = s.sale_id " +
                     "JOIN products p ON si.product_id = p.product_id " +
                     "WHERE DATE(s.sale_datetime) = CURDATE() " +
                     "GROUP BY si.product_id, p.product_name " +
                     "ORDER BY total_qty DESC LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("product_name");
                row[1] = rs.getInt("total_qty");
                topProducts.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error getting top products: " + e.getMessage());
        }
        
        return topProducts;
    }
    
    /**
     * Get sale items for a specific sale
     * @param saleId the sale ID
     * @return List of SaleItem objects
     */
    public List<SaleItem> getSaleItems(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT si.*, p.product_name, p.product_code " +
                     "FROM sale_items si JOIN products p ON si.product_id = p.product_id " +
                     "WHERE si.sale_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                SaleItem item = new SaleItem();
                item.setSaleItemId(rs.getInt("sale_item_id"));
                item.setSaleId(rs.getInt("sale_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setProductCode(rs.getString("product_code"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setPurchasePrice(rs.getDouble("purchase_price"));
                item.setLineTotal(rs.getDouble("line_total"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting sale items: " + e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Map ResultSet to Sale object
     */
    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));
        sale.setUserId(rs.getInt("user_id"));
        sale.setCashierName(rs.getString("cashier_name"));
        sale.setSaleDatetime(rs.getTimestamp("sale_datetime"));
        sale.setTotalAmount(rs.getDouble("total_amount"));
        sale.setDiscountAmount(rs.getDouble("discount_amount"));
        sale.setFinalAmount(rs.getDouble("final_amount"));
        sale.setNotes(rs.getString("notes"));
        try {
            sale.setItemCount(rs.getInt("item_count"));
        } catch (SQLException ignore) {
            // column might not be requested
        }
        return sale;
    }
    
    /**
     * Get all-time total revenue (sum of all final_amount)
     */
    public double getAllTimeTotalSales() {
        String sql = "SELECT COALESCE(SUM(final_amount), 0) FROM sales";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error getting all-time sales: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get all-time gross profit from sales (before restocking costs)
     */
    public double getAllTimeProfit() {
        String sql = "SELECT COALESCE(SUM((si.unit_price - si.purchase_price) * si.quantity), 0) FROM sale_items si";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error getting all-time profit: " + e.getMessage());
        }
        return 0;
    }
}
