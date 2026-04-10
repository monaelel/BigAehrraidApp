package com.example.bigaehrraidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onDelete(String productId);
        void onEdit(Map<String, Object> product);
        void onToggle(String productId, boolean newAvailable, Map<String, Object> productData, int flatPosition);
    }

    private final List<StoreItem> items;
    private final Listener listener;

    public StoreAdapter(List<StoreItem> items, Listener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @Override
    public int getItemCount() { return items.size(); }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == StoreItem.TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_store_header, parent, false);
            return new HeaderViewHolder(v);
        }
        View v = inflater.inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StoreItem item = items.get(position);
        if (item.type == StoreItem.TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind(item.headerName);
        } else {
            ((ProductViewHolder) holder).bind(item.product, position);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView tvHeader;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvSectionHeader);
        }
        void bind(String name) { tvHeader.setText(name); }
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvStocks, tvPrice;
        final ImageView ivImage;
        final Button btnDelete, btnEdit, btnToggle;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvProductName);
            tvStocks  = itemView.findViewById(R.id.tvProductStocks);
            tvPrice   = itemView.findViewById(R.id.tvProductPrice);
            ivImage   = itemView.findViewById(R.id.ivProductImage);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
            btnEdit   = itemView.findViewById(R.id.btnEditProduct);
            btnToggle = itemView.findViewById(R.id.btnToggleAvailable);
        }

        void bind(Map<String, Object> product, int flatPosition) {
            String name     = (String) product.getOrDefault("name", "Unnamed");
            String imageUrl = (String) product.get("imageUrl");
            Object priceObj = product.get("price");
            Object stockObj = product.get("stock");
            Object availObj = product.get("available");

            double priceValue = priceObj instanceof Number ? ((Number) priceObj).doubleValue() : 0;
            boolean available = !Boolean.FALSE.equals(availObj);

            tvName.setText(name);
            tvStocks.setText(stockObj instanceof Number
                    ? "Stocks: " + ((Number) stockObj).intValue() + " units"
                    : "Stocks: —");
            tvPrice.setText(String.format(Locale.getDefault(), "Price: $%.2f / unit", priceValue));

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext()).load(imageUrl).centerCrop()
                        .placeholder(android.R.color.darker_gray).into(ivImage);
            } else {
                ivImage.setImageResource(android.R.color.darker_gray);
            }

            applyAvailableState(available);

            btnDelete.setOnClickListener(v -> {
                String productId = (String) product.get("productId");
                if (listener != null && productId != null) listener.onDelete(productId);
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(product);
            });

            btnToggle.setOnClickListener(v -> {
                boolean newAvailable = !Boolean.TRUE.equals(product.get("available"));
                product.put("available", newAvailable);
                applyAvailableState(newAvailable);
                String productId = (String) product.get("productId");
                if (listener != null && productId != null) {
                    listener.onToggle(productId, newAvailable, product, getAdapterPosition());
                }
            });
        }

        void applyAvailableState(boolean available) {
            if (available) {
                btnToggle.setText("Available");
                btnToggle.setTextColor(0xFF000000);
                btnToggle.setBackgroundResource(R.drawable.shape_btn_outlined);
            } else {
                btnToggle.setText("Not Available");
                btnToggle.setTextColor(0xFFFFFFFF);
                btnToggle.setBackgroundResource(R.drawable.shape_btn_filled_dark);
            }
        }
    }
}
