package com.example.inventorymanager.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Professional Product Entity with extended attributes for Inventory Management.
 */
@Entity(tableName = "products")
public class Product {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String sku;            // Stock Keeping Unit / Product Code
    private String barcode;
    private String category;
    private String description;
    private double buyingPrice;
    private double sellingPrice;
    private int quantity;
    private int minimumStock;
    private String unit;           // kg, box, bottle, item, etc.
    private String supplier;       // Supplier Name
    private long dateAdded;        // Timestamp
    private long expiryDate;       // Optional Timestamp
    private String imagePath;      // Local path to product image
    private String status;         // Active, Discontinued, Out of Stock

    public Product(String name, String sku, String barcode, String category, 
                   String description, double buyingPrice, double sellingPrice, 
                   int quantity, int minimumStock, String unit, String supplier, 
                   long dateAdded, long expiryDate, String imagePath, String status) {
        this.name = name;
        this.sku = sku;
        this.barcode = barcode;
        this.category = category;
        this.description = description;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.minimumStock = minimumStock;
        this.unit = unit;
        this.supplier = supplier;
        this.dateAdded = dateAdded;
        this.expiryDate = expiryDate;
        this.imagePath = imagePath;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getBuyingPrice() { return buyingPrice; }
    public void setBuyingPrice(double buyingPrice) { this.buyingPrice = buyingPrice; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    public long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
