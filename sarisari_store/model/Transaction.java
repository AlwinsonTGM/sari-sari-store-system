package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Model Class
 * Represents a sales transaction (header)
 * Renamed from Sale to Transaction for clarity
 * Removed userId - simplified system has no user table
 */
public class Transaction {
    private int transactionId;
    private Timestamp transactionDatetime;
    private double totalAmount;
    private double discountAmount;
    private double finalAmount;
    private String notes;
    private List<TransactionItem> items;  // Line items
    private int dbItemCount = 0; // Holds item count from DB when items are not fully loaded 
    
    // Default constructor
    public Transaction() {
        this.items = new ArrayList<>();
    }
    
    // Constructor for new transactions
    public Transaction(double discountAmount, String notes) {
        this.discountAmount = discountAmount;
        this.notes = notes;
        this.items = new ArrayList<>();
        this.transactionDatetime = new Timestamp(System.currentTimeMillis());
    }
    
    // Full constructor
    public Transaction(int transactionId, Timestamp transactionDatetime,
                double totalAmount, double discountAmount, double finalAmount, String notes) {
        this.transactionId = transactionId;
        this.transactionDatetime = transactionDatetime;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.notes = notes;
        this.items = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
    
    public Timestamp getTransactionDatetime() {
        return transactionDatetime;
    }
    
    public void setTransactionDatetime(Timestamp transactionDatetime) {
        this.transactionDatetime = transactionDatetime;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public double getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public double getFinalAmount() {
        return finalAmount;
    }
    
    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<TransactionItem> getItems() {
        return items;
    }
    
    public void setItems(List<TransactionItem> items) {
        this.items = items;
    }
    
    /**
     * Add an item to the transaction
     * @param item the TransactionItem to add
     */
    public void addItem(TransactionItem item) {
        this.items.add(item);
        recalculateTotals();
    }
    
    /**
     * Remove an item from the transaction
     * @param item the TransactionItem to remove
     */
    public void removeItem(TransactionItem item) {
        this.items.remove(item);
        recalculateTotals();
    }
    
    /**
     * Clear all items
     */
    public void clearItems() {
        this.items.clear();
        recalculateTotals();
    }
    
    /**
     * Recalculate total amounts based on items
     */
    public void recalculateTotals() {
        this.totalAmount = 0;
        for (TransactionItem item : items) {
            this.totalAmount += item.getItemTotal();
        }
        this.finalAmount = this.totalAmount - this.discountAmount;
        if (this.finalAmount < 0) {
            this.finalAmount = 0;
        }
    }
    
    /**
     * Calculate total profit for this transaction
     * @return total profit
     */
    public double getTotalProfit() {
        double profit = 0;
        for (TransactionItem item : items) {
            profit += item.getProfit();
        }
        return profit - discountAmount;  // Subtract discount from profit
    }
    
    /**
     * Get number of items in the transaction
     * @return item count
     */
    public int getItemCount() {
        if (!items.isEmpty()) return items.size();
        return dbItemCount;
    }
    
    public void setItemCount(int dbItemCount) {
        this.dbItemCount = dbItemCount;
    }
    
    @Override
    public String toString() {
        return "Transaction #" + transactionId + " - " + transactionDatetime + " - P" + String.format("%.2f", finalAmount);
    }
}
