package com.example.inventorymanager.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SupplierDao {
    @Insert
    void insert(Supplier supplier);

    @Update
    void update(Supplier supplier);

    @Delete
    void delete(Supplier supplier);

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    LiveData<List<Supplier>> getAllSuppliers();
}
