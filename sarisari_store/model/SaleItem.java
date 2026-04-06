package model;

/**
 * SaleItem Model Class
 * Represents a line item in a sales transaction
 */
public class SaleItem {
    private int saleItemId;
    private int saleId;
    private int productId;
    private String productName;  // For display purposes
    private String productCode;  // For display purposes
    private int quantity;
    private double unitPrice;      // SRP at time of sale
    private double purchasePrice;  // Cost at time of sale (for profit calculation)
    private double lineTotal;
    
    // Default constructor
    public SaleItem() {
    }
    
    // Constructor for new sale items
    public SaleItem(int productId, String productName, String productCode, 
                    int quantity, double unitPrice, double purchasePrice) {
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.purchasePrice = purchasePrice;
        this.lineTotal = quantity * unitPrice;
    }
    
    // Full constructor
    public SaleItem(int saleItemId, int saleId, int productId, String productName,
                    String productCode, int quantity, double unitPrice, 
                    double purchasePrice, double lineTotal) {
        this.saleItemId = saleItemId;
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.purchasePrice = purchasePrice;
        this.lineTotal = lineTotal;
    }
    
    // Getters and Setters
    public int getSaleItemId() {
        return saleItemId;
    }
    
    public void setSaleItemId(int saleItemId) {
        this.saleItemId = saleItemId;
    }
    
    public int getSaleId() {
        return saleId;
    }
    
    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }
    
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
    
    public String getProductCode() {
        return productCode;
    }
    
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.lineTotal = quantity * unitPrice;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.lineTotal = quantity * unitPrice;
    }
    
    public double getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public double getLineTotal() {
        return lineTotal;
    }
    
    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }
    
    /**
     * Calculate profit for this line item
     * @return profit (unit profit * quantity)
     */
    public double getProfit() {
        return (unitPrice - purchasePrice) * quantity;
    }
    
    /**
     * Get profit per unit
     * @return profit per unit
     */
    public double getProfitPerUnit() {
        return unitPrice - purchasePrice;
    }
    
    @Override
    public String toString() {
        return productName + " x " + quantity + " = P" + String.format("%.2f", lineTotal);
    }
}
