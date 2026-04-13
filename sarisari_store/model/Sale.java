package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Sale Model Class
 * Represents a sales transaction (header)
 */
public class Sale {
    private int saleId;
    private int userId;
    private String cashierName;  // For display purposes
    private Timestamp saleDatetime;
    private double totalAmount;
    private double discountAmount;
    private double finalAmount;
    private String notes;
    private List<SaleItem> items;  // Line items
    private int dbItemCount = 0; // Holds item count from DB when items are not fully loaded 
    
    // Default constructor
    public Sale() {
        this.items = new ArrayList<>();
    }
    
    // Constructor for new sales
    public Sale(int userId, double discountAmount, String notes) {
        this.userId = userId;
        this.discountAmount = discountAmount;
        this.notes = notes;
        this.items = new ArrayList<>();
        this.saleDatetime = new Timestamp(System.currentTimeMillis());
    }
    
    // Full constructor
    public Sale(int saleId, int userId, String cashierName, Timestamp saleDatetime,
                double totalAmount, double discountAmount, double finalAmount, String notes) {
        this.saleId = saleId;
        this.userId = userId;
        this.cashierName = cashierName;
        this.saleDatetime = saleDatetime;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.notes = notes;
        this.items = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getSaleId() {
        return saleId;
    }
    
    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getCashierName() {
        return cashierName;
    }
    
    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }
    
    public Timestamp getSaleDatetime() {
        return saleDatetime;
    }
    
    public void setSaleDatetime(Timestamp saleDatetime) {
        this.saleDatetime = saleDatetime;
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
    
    public List<SaleItem> getItems() {
        return items;
    }
    
    public void setItems(List<SaleItem> items) {
        this.items = items;
    }
    
    /**
     * Add an item to the sale
     * @param item the SaleItem to add
     */
    public void addItem(SaleItem item) {
        this.items.add(item);
        recalculateTotals();
    }
    
    /**
     * Remove an item from the sale
     * @param item the SaleItem to remove
     */
    public void removeItem(SaleItem item) {
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
        for (SaleItem item : items) {
            this.totalAmount += item.getLineTotal();
        }
        this.finalAmount = this.totalAmount - this.discountAmount;
        if (this.finalAmount < 0) {
            this.finalAmount = 0;
        }
    }
    
    /**
     * Calculate total profit for this sale
     * @return total profit
     */
    public double getTotalProfit() {
        double profit = 0;
        for (SaleItem item : items) {
            profit += item.getProfit();
        }
        return profit - discountAmount;  // Subtract discount from profit
    }
    
    /**
     * Get number of items in the sale
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
        return "Sale #" + saleId + " - " + saleDatetime + " - P" + String.format("%.2f", finalAmount);
    }
}
