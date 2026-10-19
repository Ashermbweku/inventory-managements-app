package com.example.inventorymanager.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StockMovementDao {
    @Insert
    void insert(StockMovement movement);

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    LiveData<List<StockMovement>> getAllMovements();

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    LiveData<List<StockMovement>> getMovementsForProduct(int productId);
}
