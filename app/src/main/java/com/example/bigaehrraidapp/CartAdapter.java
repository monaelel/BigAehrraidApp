package com.example.bigaehrraidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnQuantityChanged {
        void onIncrement(String productId);
        void onDecrement(String productId);
    }

    private final List<CartItem>     items;
    private final OnQuantityChanged  listener;

    public CartAdapter(List<CartItem> items, OnQuantityChanged listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CartItem item = items.get(position);
        h.tvName.setText(item.name);
        h.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.price));
        h.tvQty.setText(String.valueOf(item.quantity));
        h.tvLineTotal.setText(String.format(Locale.getDefault(), "$%.2f", item.lineTotal()));

        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Glide.with(h.itemView.getContext()).load(item.imageUrl)
                 .centerCrop().placeholder(android.R.color.darker_gray).into(h.ivImage);
        } else {
            h.ivImage.setImageResource(android.R.color.darker_gray);
        }

        h.btnPlus.setOnClickListener(v -> listener.onIncrement(item.productId));
        h.btnMinus.setOnClickListener(v -> listener.onDecrement(item.productId));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView   ivImage;
        final TextView    tvName, tvPrice, tvQty, tvLineTotal;
        final ImageButton btnPlus, btnMinus;

        ViewHolder(@NonNull View v) {
            super(v);
            ivImage     = v.findViewById(R.id.ivCartImage);
            tvName      = v.findViewById(R.id.tvCartName);
            tvPrice     = v.findViewById(R.id.tvCartPrice);
            tvQty       = v.findViewById(R.id.tvCartQty);
            tvLineTotal = v.findViewById(R.id.tvCartLineTotal);
            btnPlus     = v.findViewById(R.id.btnCartPlus);
            btnMinus    = v.findViewById(R.id.btnCartMinus);
        }
    }
}
