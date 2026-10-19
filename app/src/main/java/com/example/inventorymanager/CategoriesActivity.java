package com.example.inventorymanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.inventorymanager.database.Category;
import com.example.inventorymanager.databinding.ActivityCategoriesBinding;
import com.example.inventorymanager.databinding.DialogAddCategoryBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class CategoriesActivity extends AppCompatActivity {

    private ActivityCategoriesBinding binding;
    private InventoryViewModel viewModel;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        adapter = new CategoryAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        viewModel.getAllCategories().observe(this, categories -> {
            adapter.submitList(new ArrayList<>(categories));
        });

        binding.fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void showAddCategoryDialog() {
        DialogAddCategoryBinding dialogBinding = DialogAddCategoryBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Category")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = dialogBinding.editTextCategoryName.getText().toString().trim();
                    String description = dialogBinding.editTextCategoryDescription.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    viewModel.insertCategory(new Category(name, description));
                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
