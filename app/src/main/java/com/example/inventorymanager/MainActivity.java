package com.example.inventorymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorymanager.database.Product;
import com.example.inventorymanager.database.StockMovement;
import com.example.inventorymanager.databinding.ActivityMainBinding;
import com.example.inventorymanager.databinding.DialogAddProductBinding;
import com.example.inventorymanager.databinding.DialogManageStockBinding;
import com.example.inventorymanager.databinding.ItemDashboardRowBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Professional Dashboard for Inventory Manager Pro.
 * Features: Summary Stats, Dynamic Greeting, Low Stock Alerts, Recent Activity, and Search.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private InventoryViewModel viewModel;
    private ProductAdapter adapter;
    private String userRole;
    private String username;
    private List<Product> allProductsList = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Receive Session Information from LoginActivity
        userRole = getIntent().getStringExtra("USER_ROLE");
        username = getIntent().getStringExtra("USERNAME");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Material 3 Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        updateGreeting();
        setupRecyclerView();
        setupViewModel();
        setupModuleNavigation();
        setupFilterChips();

        binding.fabAddProduct.setOnClickListener(view -> showAddProductDialog(null));
    }

    private void updateGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting = (timeOfDay < 12) ? "Good Morning" : (timeOfDay < 16) ? "Good Afternoon" : "Good Evening";
        binding.textGreeting.setText(String.format("%s, %s", greeting, username != null ? username : "Admin"));
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Reactive Dashboard Updates
        viewModel.getAllProducts().observe(this, products -> {
            if (products == null) return;
            allProductsList = products;
            applyFilters();
            
            binding.textTotalProducts.setText(String.valueOf(products.size()));
            
            int totalItems = 0;
            double totalValue = 0;
            for (Product p : products) {
                totalItems += p.getQuantity();
                totalValue += (p.getQuantity() * p.getSellingPrice());
            }
            
            binding.textStockItems.setText(String.valueOf(totalItems));
            binding.textStockValue.setText(String.format(Locale.getDefault(), "$%,.2f", totalValue));
            
            binding.recyclerView.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
            binding.emptyStateView.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getLowStockProducts().observe(this, this::populateLowStockPreview);
        viewModel.getAllMovements().observe(this, this::populateRecentActivity);
    }

    private void populateLowStockPreview(List<Product> products) {
        if (products == null) return;
        binding.textLowStock.setText(String.valueOf(products.size()));
        binding.layoutLowStockItems.removeAllViews();
        
        if (products.isEmpty()) {
            binding.cardLowStock.setVisibility(View.GONE);
            return;
        }
        
        binding.cardLowStock.setVisibility(View.VISIBLE);
        int count = 0;
        for (Product p : products) {
            if (count >= 3) break;
            ItemDashboardRowBinding rowBinding = ItemDashboardRowBinding.inflate(getLayoutInflater(), binding.layoutLowStockItems, true);
            rowBinding.textLabel.setText(p.getName());
            rowBinding.textValue.setText(getString(R.string.remaining_format, p.getQuantity(), p.getUnit()));
            rowBinding.textValue.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            count++;
        }
    }

    private void populateRecentActivity(List<StockMovement> movements) {
        if (movements == null) return;
        binding.layoutRecentActivity.removeAllViews();
        
        if (movements.isEmpty()) {
            binding.cardRecentActivity.setVisibility(View.GONE);
            return;
        }

        binding.cardRecentActivity.setVisibility(View.VISIBLE);
        int count = 0;
        for (StockMovement m : movements) {
            if (count >= 5) break;
            ItemDashboardRowBinding rowBinding = ItemDashboardRowBinding.inflate(getLayoutInflater(), binding.layoutRecentActivity, true);
            String prefix = m.getType().equals("IN") ? "+" : "-";
            rowBinding.textLabel.setText(String.format("%s%d %s", prefix, m.getQuantity(), m.getProductName()));
            rowBinding.textValue.setText(m.getType());
            rowBinding.textLabel.setTextColor(getResources().getColor(m.getType().equals("IN") ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
            count++;
        }
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        adapter.setOnProductClickListener(this::showProductActionDialog);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v, @NonNull RecyclerView.ViewHolder t) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if ("ADMIN".equals(userRole)) {
                    Product product = adapter.getItemAt(viewHolder.getAdapterPosition());
                    viewModel.deleteProduct(product);
                    Snackbar.make(binding.getRoot(), R.string.product_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> viewModel.insertProduct(product)).show();
                } else {
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    Toast.makeText(MainActivity.this, "Admin access required", Toast.LENGTH_SHORT).show();
                }
            }
        }).attachToRecyclerView(binding.recyclerView);
    }

    private void showProductActionDialog(Product product) {
        String[] actions = {"Update Stock", "Edit Details", "Delete"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(product.getName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) showManageStockDialog(product);
                    else if (which == 1) showAddProductDialog(product);
                    else if (which == 2) {
                        if ("ADMIN".equals(userRole)) viewModel.deleteProduct(product);
                        else Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void showManageStockDialog(Product product) {
        DialogManageStockBinding stockBinding = DialogManageStockBinding.inflate(getLayoutInflater());
        stockBinding.textProductInfo.setText(String.format("%s (Stock: %d %s)", product.getName(), product.getQuantity(), product.getUnit()));
        String[] outReasons = {"Sale", "Internal Use", "Damaged", "Expired", "Transfer", "Return", "Other"};
        stockBinding.spinnerReason.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, outReasons));
        stockBinding.toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) stockBinding.layoutReason.setVisibility(checkedId == R.id.btn_stock_out ? View.VISIBLE : View.GONE);
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Inventory Update")
                .setView(stockBinding.getRoot())
                .setPositiveButton("Process", (dialog, which) -> {
                    try {
                        int qty = Integer.parseInt(stockBinding.editTextQty.getText().toString());
                        String ref = stockBinding.editTextReference.getText().toString();
                        String user = username != null ? username : "Admin";
                        if (stockBinding.toggleGroupType.getCheckedButtonId() == R.id.btn_stock_in) {
                            viewModel.stockIn(product, qty, "Purchase", ref, user);
                        } else {
                            if (qty > product.getQuantity()) Toast.makeText(this, R.string.insufficient_stock, Toast.LENGTH_SHORT).show();
                            else viewModel.stockOut(product, qty, stockBinding.spinnerReason.getText().toString(), ref, user);
                        }
                    } catch (Exception e) { Toast.makeText(this, "Error processing update", Toast.LENGTH_SHORT).show(); }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private void showAddProductDialog(Product existing) {
        DialogAddProductBinding dialogBinding = DialogAddProductBinding.inflate(getLayoutInflater());
        if (existing != null) {
            dialogBinding.editTextName.setText(existing.getName());
            dialogBinding.editTextQuantity.setText(String.valueOf(existing.getQuantity()));
            dialogBinding.editTextSellingPrice.setText(String.valueOf(existing.getSellingPrice()));
            dialogBinding.editTextCategory.setText(existing.getCategory());
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? R.string.add_product : R.string.edit_product)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    try {
                        String name = dialogBinding.editTextName.getText().toString().trim();
                        int qty = Integer.parseInt(dialogBinding.editTextQuantity.getText().toString());
                        double price = Double.parseDouble(dialogBinding.editTextSellingPrice.getText().toString());
                        if (name.isEmpty()) return;
                        if (existing == null) {
                            viewModel.insertProduct(new Product(name, "SKU-" + (System.currentTimeMillis() % 1000), "", 
                                    dialogBinding.editTextCategory.getText().toString(), "Manual Entry", price * 0.8, price, qty, 5, "pcs", "General", System.currentTimeMillis(), 0, "", "Active"));
                        } else {
                            existing.setName(name); existing.setQuantity(qty); existing.setSellingPrice(price);
                            viewModel.updateProduct(existing);
                        }
                    } catch (Exception e) { Toast.makeText(this, "Validation error", Toast.LENGTH_SHORT).show(); }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private void setupModuleNavigation() {
        binding.btnReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        binding.btnCategories.setOnClickListener(v -> startActivity(new Intent(this, CategoriesActivity.class)));
        binding.btnSuppliers.setOnClickListener(v -> Toast.makeText(this, "Suppliers module coming soon", Toast.LENGTH_SHORT).show());
        binding.btnSettings.setOnClickListener(v -> Toast.makeText(this, "Settings module coming soon", Toast.LENGTH_SHORT).show());
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> { currentFilter = "ALL"; applyFilters(); });
        binding.chipInStock.setOnClickListener(v -> { currentFilter = "IN_STOCK"; applyFilters(); });
        binding.chipLowStockFilter.setOnClickListener(v -> { currentFilter = "LOW_STOCK"; applyFilters(); });
        binding.chipOutOfStock.setOnClickListener(v -> { currentFilter = "OUT_OF_STOCK"; applyFilters(); });
    }

    private void applyFilters() {
        List<Product> filteredList = new ArrayList<>();
        if (allProductsList == null) return;
        for (Product product : allProductsList) {
            boolean matchesSearch = product.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) || 
                                    product.getSku().toLowerCase().contains(currentSearchQuery.toLowerCase());
            boolean matchesFilter = currentFilter.equals("ALL") ||
                    (currentFilter.equals("IN_STOCK") && product.getQuantity() > product.getMinimumStock()) ||
                    (currentFilter.equals("LOW_STOCK") && product.getQuantity() <= product.getMinimumStock() && product.getQuantity() > 0) ||
                    (currentFilter.equals("OUT_OF_STOCK") && product.getQuantity() <= 0);

            if (matchesSearch && matchesFilter) filteredList.add(product);
        }
        adapter.submitList(filteredList);
    }

    private void seedDemoData() {
        // Seeding full list as requested
        viewModel.insertProduct(new Product("Coca-Cola 500ml", "COKE-001", "", "Beverages", "Soft drink", 0.90, 1.50, 100, 10, "bottles", "Coca-Cola Dist", System.currentTimeMillis(), 0, "", "Active"));
        viewModel.insertProduct(new Product("Sugar 1kg", "SUG-001", "", "Grocery", "Fine white sugar", 1.20, 2.00, 200, 20, "kg", "Sugar Mill Inc", System.currentTimeMillis(), 0, "", "Active"));
        viewModel.insertProduct(new Product("Salt 500g", "SLT-001", "", "Grocery", "Iodized salt", 0.30, 0.50, 150, 30, "packs", "Sea Salt Co", System.currentTimeMillis(), 0, "", "Active"));
        viewModel.insertProduct(new Product("Flour 2kg", "FLR-001", "", "Grocery", "Wheat flour", 2.10, 3.50, 80, 15, "bags", "Flour Mills", System.currentTimeMillis(), 0, "", "Active"));
        viewModel.insertProduct(new Product("Rice 5kg", "RIC-001", "", "Grocery", "Basmati rice", 8.00, 12.00, 8, 10, "bags", "Rice Farm", System.currentTimeMillis(), 0, "", "Active"));
        viewModel.insertProduct(new Product("Sanitary Pads", "PAD-001", "", "Personal Care", "Hygiene", 2.50, 4.00, 60, 12, "packs", "Care Corp", System.currentTimeMillis(), 0, "", "Active"));
        viewModel.insertProduct(new Product("Tissues 10pk", "TIS-001", "", "Personal Care", "Soft tissues", 0.60, 1.00, 150, 25, "packs", "Paper Soft", System.currentTimeMillis(), 0, "", "Active"));
        viewModel.insertProduct(new Product("Cement 50kg", "CMT-001", "", "Hardware", "Portland cement", 7.50, 10.00, 50, 5, "bags", "Build Pro", System.currentTimeMillis(), 0, "", "Active"));
        viewModel.insertProduct(new Product("Blue Jeans", "CLO-JNS", "", "Clothes", "Straight fit", 15.00, 25.00, 30, 8, "pairs", "Style Co", System.currentTimeMillis(), 0, "", "Active"));
        viewModel.insertProduct(new Product("White Shirt", "CLO-SRT", "", "Clothes", "Cotton", 9.00, 15.00, 45, 10, "items", "Style Co", System.currentTimeMillis(), 0, "", "Active"));

        Toast.makeText(this, "Market inventory seeded successfully", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String q) { return false; }
                @Override
                public boolean onQueryTextChange(String newText) {
                    currentSearchQuery = newText; applyFilters(); return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class)); finish(); return true;
        } else if (id == R.id.action_seed_data) {
            seedDemoData(); return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, ReportsActivity.class)); return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
