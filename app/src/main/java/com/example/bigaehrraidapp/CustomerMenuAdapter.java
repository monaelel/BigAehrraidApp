package com.example.bigaehrraidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<StoreItem> items;

    public CustomerMenuAdapter(List<StoreItem> items) {
        this.items = items;
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
            return new HeaderVH(v);
        }
        View v = inflater.inflate(R.layout.item_customer_product, parent, false);
        return new ProductVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StoreItem item = items.get(position);
        if (item.type == StoreItem.TYPE_HEADER) {
            ((HeaderVH) holder).bind(item.headerName);
        } else {
            ((ProductVH) holder).bind(item.product);
        }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView tvHeader;
        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvSectionHeader);
        }
        void bind(String name) { tvHeader.setText(name); }
    }

    static class ProductVH extends RecyclerView.ViewHolder {
        final TextView tvName, tvPrice, tvAvailability;
        final ImageView ivImage;

        ProductVH(@NonNull View itemView) {
            super(itemView);
            tvName         = itemView.findViewById(R.id.tvProductName);
            tvPrice        = itemView.findViewById(R.id.tvProductPrice);
            tvAvailability = itemView.findViewById(R.id.tvProductAvailability);
            ivImage        = itemView.findViewById(R.id.ivProductImage);
        }

        void bind(Map<String, Object> product) {
            String name     = (String) product.getOrDefault("name", "Unnamed");
            String imageUrl = (String) product.get("imageUrl");
            Object priceObj = product.get("price");
            Object availObj = product.get("available");

            double price    = priceObj instanceof Number ? ((Number) priceObj).doubleValue() : 0;
            boolean available = !Boolean.FALSE.equals(availObj);

            tvName.setText(name);
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", price));
            tvAvailability.setText(available ? "Available" : "Not available");
            tvAvailability.setTextColor(available ? 0xFF2E7D32 : 0xFFB71C1C);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext()).load(imageUrl).centerCrop()
                        .placeholder(android.R.color.darker_gray).into(ivImage);
            } else {
                ivImage.setImageResource(android.R.color.darker_gray);
            }
        }
    }
}
