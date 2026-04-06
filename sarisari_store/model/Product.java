package model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Product Model Class
 * Represents a product in the inventory
 */
public class Product {
    private int productId;
    private String productCode;
    private String productName;
    private String category;
    private String unit;
    private double purchasePrice;
    private double srp;
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
    public Product(String productCode, String productName, String category, String unit,
                   double purchasePrice, double srp, int currentStock, int minStockLevel,
                   Date expiryDate, String imagePath) {
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.unit = unit;
        this.purchasePrice = purchasePrice;
        this.srp = srp;
        this.currentStock = currentStock;
        this.minStockLevel = minStockLevel;
        this.expiryDate = expiryDate;
        this.imagePath = imagePath;
        this.isActive = true;
    }
    
    // Full constructor
    public Product(int productId, String productCode, String productName, String category, 
                   String unit, double purchasePrice, double srp, int currentStock, 
                   int minStockLevel, Date expiryDate, String imagePath, boolean isActive,
                   Timestamp createdAt, Timestamp updatedAt) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.unit = unit;
        this.purchasePrice = purchasePrice;
        this.srp = srp;
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
    
    public String getProductCode() {
        return productCode;
    }
    
    public void setProductCode(String productCode) {
        this.productCode = productCode;
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
    
    public double getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public double getSrp() {
        return srp;
    }
    
    public void setSrp(double srp) {
        this.srp = srp;
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
     * @return profit (SRP - purchase price)
     */
    public double getProfitPerUnit() {
        return srp - purchasePrice;
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
        return productName + " (" + productCode + ")";
    }
}
