package com.example.inventorymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorymanager.database.Product;
import com.example.inventorymanager.databinding.ItemProductBinding;

import java.util.Locale;

public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ProductViewHolder> {

    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public ProductAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getQuantity() == newItem.getQuantity() &&
                    oldItem.getSku().equals(newItem.getSku()) &&
                    Double.compare(oldItem.getSellingPrice(), newItem.getSellingPrice()) == 0;
        }
    };

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product currentProduct = getItem(position);
        Context context = holder.itemView.getContext();

        holder.binding.textViewName.setText(currentProduct.getName());
        holder.binding.textViewSku.setText(context.getString(R.string.sku_format, currentProduct.getSku()));
        holder.binding.chipCategory.setText(currentProduct.getCategory());
        holder.binding.textViewQuantity.setText(String.valueOf(currentProduct.getQuantity()));
        holder.binding.textViewUnit.setText(currentProduct.getUnit());
        
        String priceFormatted = String.format(Locale.getDefault(), context.getString(R.string.price_format), currentProduct.getSellingPrice());
        holder.binding.textViewPrice.setText(priceFormatted);
        
        // Professional low stock warning
        if (currentProduct.getQuantity() <= currentProduct.getMinimumStock()) {
            holder.binding.textViewLowStock.setVisibility(View.VISIBLE);
            holder.binding.textViewQuantity.setTextColor(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_error));
        } else {
            holder.binding.textViewLowStock.setVisibility(View.GONE);
            holder.binding.textViewQuantity.setTextColor(ContextCompat.getColor(context, R.color.primary));
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onProductClick(getItem(pos));
            }
        });
    }

    public Product getItemAt(int position) {
        return getItem(position);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        public ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
