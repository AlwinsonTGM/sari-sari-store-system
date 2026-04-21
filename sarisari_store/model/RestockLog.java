package model;

import java.sql.Timestamp;

/**
 * RestockLog Model Class
 * Tracks all stock additions and their capital cost
 * Removed userId - simplified system has no user table
 * Renamed purchasePrice to costPerUnit for clarity
 */
public class RestockLog {
    private int logId;
    private int productId;
    private String productName;
    private int quantityAdded;
    private double costPerUnit;      // Cost per unit at time of restock
    private double totalCost;        // Total: costPerUnit × quantityAdded
    private Timestamp restockDate;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantityAdded() { return quantityAdded; }
    public void setQuantityAdded(int quantityAdded) { this.quantityAdded = quantityAdded; }

    public double getCostPerUnit() { return costPerUnit; }
    public void setCostPerUnit(double costPerUnit) { this.costPerUnit = costPerUnit; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public Timestamp getRestockDate() { return restockDate; }
    public void setRestockDate(Timestamp restockDate) { this.restockDate = restockDate; }
}
