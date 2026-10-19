package com.example.inventorymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.inventorymanager.database.Product;
import com.example.inventorymanager.database.StockMovement;
import com.example.inventorymanager.databinding.ActivityReportsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Professional Reporting Module.
 * Provides specialized views for Inventory Summary, Low Stock, Out of Stock, and Movement History.
 */
public class ReportsActivity extends AppCompatActivity {

    private ActivityReportsBinding binding;
    private ProductAdapter productAdapter;
    private MovementAdapter movementAdapter;
    
    private List<Product> allProducts = new ArrayList<>();
    private List<StockMovement> allMovements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar with professional back navigation
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.reports);
        }
        
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
        
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        setupRecyclerView();
        setupViewModel();
        setupButtons();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter();
        movementAdapter = new MovementAdapter();
        
        binding.recyclerReports.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReports.setHasFixedSize(true);
        // Default adapter is productAdapter
        binding.recyclerReports.setAdapter(productAdapter);
    }

    private void setupViewModel() {
        InventoryViewModel viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        
        // Observe Products for Valuation and status reports
        viewModel.getAllProducts().observe(this, products -> {
            if (products == null) return;
            allProducts = products;
            updateValuation(products);
            
            // Show summary by default on initial load
            if (binding.textReportTitle.getVisibility() == View.GONE) {
                showSummary();
            }
        });

        // Observe Movements for History report
        viewModel.getAllMovements().observe(this, movements -> {
            if (movements == null) return;
            allMovements = movements;
        });
    }

    private void updateValuation(List<Product> products) {
        double totalValue = 0;
        for (Product p : products) {
            totalValue += (p.getQuantity() * p.getSellingPrice());
        }
        binding.textTotalValuation.setText(String.format(Locale.getDefault(), "$%,.2f", totalValue));
    }

    private void setupButtons() {
        binding.btnReportSummary.setOnClickListener(v -> showSummary());
        binding.btnReportLowStock.setOnClickListener(v -> showLowStock());
        binding.btnReportOutOfStock.setOnClickListener(v -> showOutOfStock());
        binding.btnReportMovements.setOnClickListener(v -> showMovements());
    }

    private void showSummary() {
        binding.textReportTitle.setText(R.string.report_inventory_summary);
        binding.textReportTitle.setVisibility(View.VISIBLE);
        binding.recyclerReports.setAdapter(productAdapter);
        productAdapter.submitList(new ArrayList<>(allProducts));
    }

    private void showLowStock() {
        binding.textReportTitle.setText(R.string.report_low_stock);
        binding.textReportTitle.setVisibility(View.VISIBLE);
        binding.recyclerReports.setAdapter(productAdapter);
        
        List<Product> lowStock = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getQuantity() <= p.getMinimumStock() && p.getQuantity() > 0) {
                lowStock.add(p);
            }
        }
        productAdapter.submitList(lowStock);
        
        if (lowStock.isEmpty()) {
            Toast.makeText(this, "No low stock items found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showOutOfStock() {
        binding.textReportTitle.setText(R.string.report_out_of_stock);
        binding.textReportTitle.setVisibility(View.VISIBLE);
        binding.recyclerReports.setAdapter(productAdapter);
        
        List<Product> outOfStock = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getQuantity() <= 0) {
                outOfStock.add(p);
            }
        }
        productAdapter.submitList(outOfStock);
        
        if (outOfStock.isEmpty()) {
            Toast.makeText(this, "No out-of-stock items found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMovements() {
        binding.textReportTitle.setText(R.string.report_stock_movement);
        binding.textReportTitle.setVisibility(View.VISIBLE);
        
        // Professional Toggle: Swap to movement adapter for specialized history view
        binding.recyclerReports.setAdapter(movementAdapter);
        movementAdapter.submitList(new ArrayList<>(allMovements));
        
        if (allMovements.isEmpty()) {
            Toast.makeText(this, "No movement history recorded yet", Toast.LENGTH_SHORT).show();
        }
    }
}
