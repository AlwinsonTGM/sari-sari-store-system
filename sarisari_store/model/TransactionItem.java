package model;

/**
 * TransactionItem Model Class
 * Represents a line item in a sales transaction
 * Renamed fields for clarity: soldPrice (was unitPrice), costAtSale (was purchasePrice), itemTotal (was lineTotal)
 */
public class TransactionItem {
    private int transactionItemId;
    private int transactionId;
    private int productId;
    private String productName;  // For display purposes
    private int quantity;
    private double soldPrice;      // Actual price customer paid for this item
    private double costAtSale;     // Historical cost when this item was sold (for profit calculation)
    private double itemTotal;      // Subtotal: soldPrice × quantity
    
    // Default constructor
    public TransactionItem() {
    }
    
    // Constructor for new transaction items
    public TransactionItem(int productId, String productName, 
                    int quantity, double soldPrice, double costAtSale) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.soldPrice = soldPrice;
        this.costAtSale = costAtSale;
        this.itemTotal = quantity * soldPrice;
    }
    
    // Full constructor
    public TransactionItem(int transactionItemId, int transactionId, int productId, String productName,
                    int quantity, double soldPrice, double costAtSale, double itemTotal) {
        this.transactionItemId = transactionItemId;
        this.transactionId = transactionId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.soldPrice = soldPrice;
        this.costAtSale = costAtSale;
        this.itemTotal = itemTotal;
    }
    
    // Getters and Setters
    public int getTransactionItemId() {
        return transactionItemId;
    }
    
    public void setTransactionItemId(int transactionItemId) {
        this.transactionItemId = transactionItemId;
    }
    
    public int getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
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
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.itemTotal = quantity * soldPrice;
    }
    
    public double getSoldPrice() {
        return soldPrice;
    }
    
    public void setSoldPrice(double soldPrice) {
        this.soldPrice = soldPrice;
        this.itemTotal = quantity * soldPrice;
    }
    
    public double getCostAtSale() {
        return costAtSale;
    }
    
    public void setCostAtSale(double costAtSale) {
        this.costAtSale = costAtSale;
    }
    
    public double getItemTotal() {
        return itemTotal;
    }
    
    public void setItemTotal(double itemTotal) {
        this.itemTotal = itemTotal;
    }
    
    /**
     * Calculate profit for this line item
     * @return profit (soldPrice - costAtSale) × quantity
     */
    public double getProfit() {
        return (soldPrice - costAtSale) * quantity;
    }
    
    /**
     * Get profit per unit
     * @return profit per unit
     */
    public double getProfitPerUnit() {
        return soldPrice - costAtSale;
    }
    
    @Override
    public String toString() {
        return productName + " x " + quantity + " = P" + String.format("%.2f", itemTotal);
    }
}
