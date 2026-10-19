package com.example.inventorymanager.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Professional Inventory Database including all modules:
 * Products, Users, Categories, Suppliers, and Stock Movements.
 */
@Database(entities = {Product.class, User.class, Category.class, Supplier.class, StockMovement.class}, version = 2, exportSchema = false)
public abstract class InventoryDatabase extends RoomDatabase {

    private static InventoryDatabase instance;

    public abstract ProductDao productDao();
    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract SupplierDao supplierDao();
    public abstract StockMovementDao stockMovementDao();

    public static synchronized InventoryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            InventoryDatabase.class, "inventory_database")
                    .fallbackToDestructiveMigration() // Use Destructive Migration for development
                    .build();
        }
        return instance;
    }
}
