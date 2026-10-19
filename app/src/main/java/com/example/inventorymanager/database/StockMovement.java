package com.example.inventorymanager.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stock_movements")
public class StockMovement {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int productId;
    private String productName;
    private String type; // "IN", "OUT", "ADJUSTMENT"
    private int quantity;
    private int stockBefore;
    private int stockAfter;
    private String reason; // e.g., "Purchase", "Sale", "Damage"
    private String reference; // e.g., Invoice number
    private long timestamp;
    private String processedBy; // Username

    public StockMovement(int productId, String productName, String type, int quantity, 
                         int stockBefore, int stockAfter, String reason, 
                         String reference, long timestamp, String processedBy) {
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.stockBefore = stockBefore;
        this.stockAfter = stockAfter;
        this.reason = reason;
        this.reference = reference;
        this.timestamp = timestamp;
        this.processedBy = processedBy;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getStockBefore() { return stockBefore; }
    public void setStockBefore(int stockBefore) { this.stockBefore = stockBefore; }
    public int getStockAfter() { return stockAfter; }
    public void setStockAfter(int stockAfter) { this.stockAfter = stockAfter; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
}
