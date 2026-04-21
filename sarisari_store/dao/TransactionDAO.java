package dao;

import model.Transaction;
import model.TransactionItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAO - Data Access Object for Transaction operations
 * Replaces SaleDAO after system simplification.
 * Uses the 'transactions' and 'transaction_items' tables.
 * No user references - simplified, hardcoded authentication only.
 */
public class TransactionDAO {

    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WRITE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Create a new transaction with all its items (wrapped in a DB transaction).
     * Automatically deducts stock from products.
     *
     * @param transaction the Transaction to persist
     * @return true if successful
     */
    public boolean createTransaction(Transaction transaction) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Insert transaction header
            String txSql = "INSERT INTO transactions (total_amount, discount_amount, final_amount, notes) " +
                           "VALUES (?, ?, ?, ?)";
            PreparedStatement txStmt = conn.prepareStatement(txSql, Statement.RETURN_GENERATED_KEYS);
            txStmt.setDouble(1, transaction.getTotalAmount());
            txStmt.setDouble(2, transaction.getDiscountAmount());
            txStmt.setDouble(3, transaction.getFinalAmount());
            txStmt.setString(4, transaction.getNotes());

            if (txStmt.executeUpdate() == 0) { conn.rollback(); return false; }

            ResultSet keys = txStmt.getGeneratedKeys();
            int txId;
            if (keys.next()) {
                txId = keys.getInt(1);
                transaction.setTransactionId(txId);
            } else { conn.rollback(); return false; }

            // 2. Insert line items and deduct stock
            String itemSql = "INSERT INTO transaction_items (transaction_id, product_id, quantity, sold_price, cost_at_sale, item_total) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
            String stockSql = "UPDATE products SET current_stock = current_stock - ? " +
                              "WHERE product_id = ? AND current_stock >= ?";

            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            PreparedStatement stockStmt = conn.prepareStatement(stockSql);

            for (TransactionItem item : transaction.getItems()) {
                itemStmt.setInt(1, txId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getSoldPrice());
                itemStmt.setDouble(5, item.getCostAtSale());
                itemStmt.setDouble(6, item.getItemTotal());
                itemStmt.addBatch();

                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.setInt(3, item.getQuantity());
                int updated = stockStmt.executeUpdate();
                if (updated == 0) { conn.rollback(); return false; } // Insufficient stock
            }

            itemStmt.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* ignore */ } }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get a transaction by ID including all its line items.
     */
    public Transaction getById(int transactionId) {
        String sql = "SELECT t.*, " +
                     "(SELECT COUNT(*) FROM transaction_items ti WHERE ti.transaction_id = t.transaction_id) AS item_count " +
                     "FROM transactions t WHERE t.transaction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Transaction t = mapRow(rs);
                t.setItems(getItems(transactionId));
                return t;
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaction: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all transactions, most recent first.
     */
    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, " +
                     "(SELECT COUNT(*) FROM transaction_items ti WHERE ti.transaction_id = t.transaction_id) AS item_count " +
                     "FROM transactions t ORDER BY t.transaction_datetime DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error getting all transactions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get transactions for today.
     */
    public List<Transaction> getTodayTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, " +
                     "(SELECT COUNT(*) FROM transaction_items ti WHERE ti.transaction_id = t.transaction_id) AS item_count " +
                     "FROM transactions t WHERE DATE(t.transaction_datetime) = CURDATE() " +
                     "ORDER BY t.transaction_datetime DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error getting today's transactions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get transactions in a date range.
     */
    public List<Transaction> getByDateRange(Date fromDate, Date toDate) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, " +
                     "(SELECT COUNT(*) FROM transaction_items ti WHERE ti.transaction_id = t.transaction_id) AS item_count " +
                     "FROM transactions t " +
                     "WHERE DATE(t.transaction_datetime) BETWEEN ? AND ? " +
                     "ORDER BY t.transaction_datetime DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, fromDate);
            stmt.setDate(2, toDate);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error getting transactions by date range: " + e.getMessage());
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AGGREGATES  (used by DashboardPanel)
    // ─────────────────────────────────────────────────────────────────────────

    public double getTodayTotalSales() {
        return queryDouble("SELECT COALESCE(SUM(final_amount),0) FROM transactions WHERE DATE(transaction_datetime)=CURDATE()");
    }

    public int getTodayTransactionCount() {
        return queryInt("SELECT COUNT(*) FROM transactions WHERE DATE(transaction_datetime)=CURDATE()");
    }

    public double getTodayProfit() {
        return queryDouble(
            "SELECT COALESCE(SUM((ti.sold_price - ti.cost_at_sale) * ti.quantity),0) " +
            "FROM transaction_items ti JOIN transactions t ON ti.transaction_id=t.transaction_id " +
            "WHERE DATE(t.transaction_datetime)=CURDATE()");
    }

    public double getAllTimeTotalSales() {
        return queryDouble("SELECT COALESCE(SUM(final_amount),0) FROM transactions");
    }

    public double getAllTimeProfit() {
        return queryDouble(
            "SELECT COALESCE(SUM((sold_price - cost_at_sale) * quantity),0) FROM transaction_items");
    }

    /**
     * Get top N selling products for today.
     * @return list of Object[]{productName, totalQty}
     */
    public List<Object[]> getTopSellingProductsToday(int limit) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT p.product_name, SUM(ti.quantity) AS total_qty " +
                     "FROM transaction_items ti " +
                     "JOIN transactions t ON ti.transaction_id=t.transaction_id " +
                     "JOIN products p ON ti.product_id=p.product_id " +
                     "WHERE DATE(t.transaction_datetime)=CURDATE() " +
                     "GROUP BY ti.product_id, p.product_name " +
                     "ORDER BY total_qty DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(new Object[]{rs.getString("product_name"), rs.getInt("total_qty")});
            }
        } catch (SQLException e) {
            System.err.println("Error getting top products: " + e.getMessage());
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private List<TransactionItem> getItems(int transactionId) {
        List<TransactionItem> items = new ArrayList<>();
        String sql = "SELECT ti.*, p.product_name " +
                     "FROM transaction_items ti JOIN products p ON ti.product_id=p.product_id " +
                     "WHERE ti.transaction_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TransactionItem item = new TransactionItem();
                item.setTransactionItemId(rs.getInt("transaction_item_id"));
                item.setTransactionId(rs.getInt("transaction_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setSoldPrice(rs.getDouble("sold_price"));
                item.setCostAtSale(rs.getDouble("cost_at_sale"));
                item.setItemTotal(rs.getDouble("item_total"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaction items: " + e.getMessage());
        }
        return items;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setTransactionDatetime(rs.getTimestamp("transaction_datetime"));
        t.setTotalAmount(rs.getDouble("total_amount"));
        t.setDiscountAmount(rs.getDouble("discount_amount"));
        t.setFinalAmount(rs.getDouble("final_amount"));
        t.setNotes(rs.getString("notes"));
        try { t.setItemCount(rs.getInt("item_count")); } catch (SQLException ignore) {}
        return t;
    }

    private double queryDouble(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
        return 0.0;
    }

    private int queryInt(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
        return 0;
    }
}
