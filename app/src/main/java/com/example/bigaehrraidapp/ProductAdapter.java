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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(String productId, int position);
    }

    public interface OnEditListener {
        void onEdit(Map<String, Object> product);
    }

    public interface OnAvailableToggleListener {
        void onToggle(String productId, boolean newAvailable, int position);
    }

    private final List<Map<String, Object>> products;
    private final OnDeleteListener deleteListener;
    private final OnEditListener editListener;
    private final OnAvailableToggleListener toggleListener;

    public ProductAdapter(List<Map<String, Object>> products,
                          OnDeleteListener deleteListener,
                          OnEditListener editListener,
                          OnAvailableToggleListener toggleListener) {
        this.products       = products;
        this.deleteListener = deleteListener;
        this.editListener   = editListener;
        this.toggleListener = toggleListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> product = products.get(position);

        String name     = (String) product.getOrDefault("name", "Unnamed");
        String imageUrl = (String) product.get("imageUrl");
        Object priceObj = product.get("price");
        Object stockObj = product.get("stock");
        Object availObj = product.get("available");

        double priceValue = priceObj instanceof Number ? ((Number) priceObj).doubleValue() : 0;
        boolean available = !(Boolean.FALSE.equals(availObj));

        holder.tvName.setText(name);

        if (stockObj instanceof Number) {
            holder.tvStocks.setText("Stocks: " + ((Number) stockObj).intValue() + " units");
        } else {
            holder.tvStocks.setText("Stocks: —");
        }

        holder.tvPrice.setText(String.format(Locale.getDefault(), "Price: $%.2f / unit", priceValue));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(android.R.color.darker_gray);
            holder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        applyAvailableState(holder, available);

        holder.btnDelete.setOnClickListener(v -> {
            String productId = (String) product.get("productId");
            if (deleteListener != null && productId != null) {
                deleteListener.onDelete(productId, holder.getAdapterPosition());
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(product);
        });

        holder.btnToggle.setOnClickListener(v -> {
            String productId = (String) product.get("productId");
            if (toggleListener != null && productId != null) {
                boolean newAvailable = !Boolean.TRUE.equals(product.get("available"));
                product.put("available", newAvailable);
                applyAvailableState(holder, newAvailable);
                toggleListener.onToggle(productId, newAvailable, holder.getAdapterPosition());
            }
        });
    }

    private void applyAvailableState(ViewHolder holder, boolean available) {
        if (available) {
            holder.btnToggle.setText("Available");
            holder.btnToggle.setTextColor(0xFF000000);
            holder.btnToggle.setBackgroundResource(R.drawable.shape_btn_outlined);
        } else {
            holder.btnToggle.setText("Not Available");
            holder.btnToggle.setTextColor(0xFFFFFFFF);
            holder.btnToggle.setBackgroundResource(R.drawable.shape_btn_filled_dark);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void removeItem(int position) {
        products.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStocks, tvPrice;
        ImageView ivImage;
        Button btnDelete, btnEdit, btnToggle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvProductName);
            tvStocks  = itemView.findViewById(R.id.tvProductStocks);
            tvPrice   = itemView.findViewById(R.id.tvProductPrice);
            ivImage   = itemView.findViewById(R.id.ivProductImage);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
            btnEdit   = itemView.findViewById(R.id.btnEditProduct);
            btnToggle = itemView.findViewById(R.id.btnToggleAvailable);
        }
    }
}
