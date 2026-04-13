package model;

import java.sql.Timestamp;

public class RestockLog {
    private int logId;
    private int productId;
    private String productName;
    private int quantityAdded;
    private double purchasePrice;
    private double totalCost;
    private int userId;
    private String cashierName;
    private Timestamp restockDate;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantityAdded() { return quantityAdded; }
    public void setQuantityAdded(int quantityAdded) { this.quantityAdded = quantityAdded; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public Timestamp getRestockDate() { return restockDate; }
    public void setRestockDate(Timestamp restockDate) { this.restockDate = restockDate; }
}
