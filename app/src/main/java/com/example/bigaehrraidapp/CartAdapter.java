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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> items;
    private final OnCartItemActionListener listener;

    public interface OnCartItemActionListener {
        void onQuantityChanged(String productId, int newQuantity);
        void onRemoved(String productId);
    }

    public CartAdapter(List<CartItem> items, OnCartItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        final TextView tvName, tvPrice, tvQuantity, btnMinus, btnPlus;
        final ImageView ivImage, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartProductName);
            tvPrice = itemView.findViewById(R.id.tvCartProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            ivImage = itemView.findViewById(R.id.ivCartImage);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(CartItem item, OnCartItemActionListener listener) {
            tvName.setText(item.getName());
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(item.getImageUrl()).centerCrop()
                        .placeholder(android.R.color.darker_gray).into(ivImage);
            } else {
                ivImage.setImageResource(android.R.color.darker_gray);
            }

            btnMinus.setOnClickListener(v -> {
                int q = item.getQuantity() - 1;
                listener.onQuantityChanged(item.getProductId(), q);
            });

            btnPlus.setOnClickListener(v -> {
                int q = item.getQuantity() + 1;
                listener.onQuantityChanged(item.getProductId(), q);
            });

            btnRemove.setOnClickListener(v -> listener.onRemoved(item.getProductId()));
        }
    }
}
