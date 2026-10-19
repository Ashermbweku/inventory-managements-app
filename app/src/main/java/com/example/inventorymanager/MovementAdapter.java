package com.example.inventorymanager;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorymanager.database.StockMovement;
import com.example.inventorymanager.databinding.ItemStockMovementBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MovementAdapter extends ListAdapter<StockMovement, MovementAdapter.MovementViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public MovementAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<StockMovement> DIFF_CALLBACK = new DiffUtil.ItemCallback<StockMovement>() {
        @Override
        public boolean areItemsTheSame(@NonNull StockMovement oldItem, @NonNull StockMovement newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull StockMovement oldItem, @NonNull StockMovement newItem) {
            return oldItem.getTimestamp() == newItem.getTimestamp() &&
                    oldItem.getQuantity() == newItem.getQuantity() &&
                    oldItem.getType().equals(newItem.getType());
        }
    };

    @NonNull
    @Override
    public MovementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStockMovementBinding binding = ItemStockMovementBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MovementViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovementViewHolder holder, int position) {
        StockMovement movement = getItem(position);
        
        holder.binding.textMoveProduct.setText(movement.getProductName());
        holder.binding.textMoveDetails.setText(String.format("%s • Ref: %s", movement.getReason(), 
                movement.getReference().isEmpty() ? "N/A" : movement.getReference()));
        
        String prefix = movement.getType().equals("IN") ? "+" : "-";
        holder.binding.textMoveQty.setText(prefix + movement.getQuantity());
        
        int color = movement.getType().equals("IN") ? android.R.color.holo_green_dark : android.R.color.holo_red_dark;
        holder.binding.textMoveQty.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), color));
        
        holder.binding.textMoveStock.setText(String.format("Stock: %d -> %d", movement.getStockBefore(), movement.getStockAfter()));
        
        String dateStr = DATE_FORMAT.format(new Date(movement.getTimestamp()));
        holder.binding.textMoveTime.setText(String.format("%s • By %s", dateStr, movement.getProcessedBy()));
    }

    static class MovementViewHolder extends RecyclerView.ViewHolder {
        private final ItemStockMovementBinding binding;

        public MovementViewHolder(ItemStockMovementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
