package com.example.inventorymanager;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.inventorymanager.database.Category;
import com.example.inventorymanager.database.CategoryDao;
import com.example.inventorymanager.database.InventoryDatabase;
import com.example.inventorymanager.database.Product;
import com.example.inventorymanager.database.ProductDao;
import com.example.inventorymanager.database.StockMovement;
import com.example.inventorymanager.database.StockMovementDao;
import com.example.inventorymanager.database.Supplier;
import com.example.inventorymanager.database.SupplierDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Professional ViewModel that coordinates all modules: Products, Categories, Suppliers, and History.
 */
public class InventoryViewModel extends AndroidViewModel {

    private final ProductDao productDao;
    private final CategoryDao categoryDao;
    private final SupplierDao supplierDao;
    private final StockMovementDao movementDao;

    private final LiveData<List<Product>> allProducts;
    private final LiveData<List<Category>> allCategories;
    private final LiveData<List<Supplier>> allSuppliers;
    private final LiveData<List<StockMovement>> allMovements;
    private final LiveData<List<Product>> lowStockProducts;

    private final ExecutorService executorService;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        InventoryDatabase database = InventoryDatabase.getInstance(application);
        
        productDao = database.productDao();
        categoryDao = database.categoryDao();
        supplierDao = database.supplierDao();
        movementDao = database.stockMovementDao();

        allProducts = productDao.getAllProducts();
        allCategories = categoryDao.getAllCategories();
        allSuppliers = supplierDao.getAllSuppliers();
        allMovements = movementDao.getAllMovements();
        lowStockProducts = productDao.getLowStockProducts();
        
        executorService = Executors.newFixedThreadPool(4);
    }

    // --- Product Methods ---
    public LiveData<List<Product>> getAllProducts() { return allProducts; }
    public LiveData<List<Product>> getLowStockProducts() { return lowStockProducts; }

    public void insertProduct(Product product) {
        executorService.execute(() -> productDao.insert(product));
    }

    public void updateProduct(Product product) {
        executorService.execute(() -> productDao.update(product));
    }

    public void deleteProduct(Product product) {
        executorService.execute(() -> productDao.delete(product));
    }

    // --- Stock Movement Logic ---
    public void stockIn(Product product, int quantityReceived, String reason, String reference, String processedBy) {
        executorService.execute(() -> {
            int oldStock = product.getQuantity();
            int newStock = oldStock + quantityReceived;
            product.setQuantity(newStock);
            productDao.update(product);
            
            StockMovement movement = new StockMovement(
                    product.getId(), product.getName(), "IN", quantityReceived,
                    oldStock, newStock, reason, reference, System.currentTimeMillis(), processedBy
            );
            movementDao.insert(movement);
        });
    }

    public void stockOut(Product product, int quantityRemoved, String reason, String reference, String processedBy) {
        executorService.execute(() -> {
            int oldStock = product.getQuantity();
            int newStock = oldStock - quantityRemoved;
            product.setQuantity(newStock);
            productDao.update(product);
            
            StockMovement movement = new StockMovement(
                    product.getId(), product.getName(), "OUT", quantityRemoved,
                    oldStock, newStock, reason, reference, System.currentTimeMillis(), processedBy
            );
            movementDao.insert(movement);
        });
    }

    public void insertCategory(Category category) {
        executorService.execute(() -> categoryDao.insert(category));
    }

    public void updateCategory(Category category) {
        executorService.execute(() -> categoryDao.update(category));
    }

    public void deleteCategory(Category category) {
        executorService.execute(() -> categoryDao.delete(category));
    }

    // --- Category & Supplier ---
    public LiveData<List<Category>> getAllCategories() { return allCategories; }
    public LiveData<List<Supplier>> getAllSuppliers() { return allSuppliers; }
    public LiveData<List<StockMovement>> getAllMovements() { return allMovements; }
}
