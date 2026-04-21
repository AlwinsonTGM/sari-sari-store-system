package model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Product Model Class
 * Represents a product in the inventory
 * Note: product_code removed - using product_id as unique identifier
 * Renamed fields for clarity: costPerUnit (was purchasePrice), sellPrice (was srp)
 */
public class Product {
    private int productId;
    private String productName;
    private String category;
    private String unit;
    private double costPerUnit;   // How much you pay to buy 1 item from supplier
    private double sellPrice;     // How much you charge customers for 1 item
    private int currentStock;
    private int minStockLevel;
    private Date expiryDate;
    private String imagePath;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Default constructor
    public Product() {
    }
    
    // Constructor without ID (for new products)
    public Product(String productName, String category, String unit,
                   double costPerUnit, double sellPrice, int currentStock, int minStockLevel,
                   Date expiryDate, String imagePath) {
        this.productName = productName;
        this.category = category;
        this.unit = unit;
        this.costPerUnit = costPerUnit;
        this.sellPrice = sellPrice;
        this.currentStock = currentStock;
        this.minStockLevel = minStockLevel;
        this.expiryDate = expiryDate;
        this.imagePath = imagePath;
        this.isActive = true;
    }
    
    // Full constructor
    public Product(int productId, String productName, String category, 
                   String unit, double costPerUnit, double sellPrice, int currentStock, 
                   int minStockLevel, Date expiryDate, String imagePath, boolean isActive,
                   Timestamp createdAt, Timestamp updatedAt) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.unit = unit;
        this.costPerUnit = costPerUnit;
        this.sellPrice = sellPrice;
        this.currentStock = currentStock;
        this.minStockLevel = minStockLevel;
        this.expiryDate = expiryDate;
        this.imagePath = imagePath;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public double getCostPerUnit() {
        return costPerUnit;
    }
    
    public void setCostPerUnit(double costPerUnit) {
        this.costPerUnit = costPerUnit;
    }
    
    public double getSellPrice() {
        return sellPrice;
    }
    
    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }
    
    public int getCurrentStock() {
        return currentStock;
    }
    
    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }
    
    public int getMinStockLevel() {
        return minStockLevel;
    }
    
    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Calculate profit per unit
     * @return profit (sell price - cost per unit)
     */
    public double getProfitPerUnit() {
        return sellPrice - costPerUnit;
    }
    
    /**
     * Check if stock is low
     * @return true if current stock is at or below minimum level
     */
    public boolean isLowStock() {
        return currentStock <= minStockLevel;
    }
    
    /**
     * Check if product has expired
     * @return true if expiry date has passed
     */
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.before(new Date(System.currentTimeMillis()));
    }
    
    @Override
    public String toString() {
        return productName + " (Stock: " + currentStock + ")";
    }

    // ── Backward-compatibility aliases (old field names used by GUI) ──────────

    /** @deprecated Use getCostPerUnit() */
    public double getPurchasePrice() { return costPerUnit; }
    /** @deprecated Use setCostPerUnit() */
    public void setPurchasePrice(double v) { this.costPerUnit = v; }

    /** @deprecated Use getSellPrice() */
    public double getSrp() { return sellPrice; }
    /** @deprecated Use setSellPrice() */
    public void setSrp(double v) { this.sellPrice = v; }

    /** @deprecated product_code removed; returns empty string */
    public String getProductCode() { return ""; }
    /** @deprecated product_code removed; no-op */
    public void setProductCode(String code) { /* no-op */ }
}
