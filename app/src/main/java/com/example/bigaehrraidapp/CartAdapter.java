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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartVH> {

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    private final List<CartItem>        items;
    private final OnCartChangedListener listener;

    public CartAdapter(List<CartItem> items, OnCartChangedListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_item, parent, false);
        return new CartVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartVH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class CartVH extends RecyclerView.ViewHolder {
        final ImageView   ivImage;
        final TextView    tvName, tvPrice, tvQty;
        final ImageButton btnInc, btnDec;

        CartVH(@NonNull View itemView) {
            super(itemView);
            ivImage  = itemView.findViewById(R.id.ivCartItemImage);
            tvName   = itemView.findViewById(R.id.tvCartItemName);
            tvPrice  = itemView.findViewById(R.id.tvCartItemPrice);
            tvQty    = itemView.findViewById(R.id.tvCartItemQty);
            btnInc   = itemView.findViewById(R.id.btnIncrement);
            btnDec   = itemView.findViewById(R.id.btnDecrement);
        }

        void bind(CartItem item) {
            tvName.setText(item.name);
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f each", item.price));
            tvQty.setText(String.valueOf(item.quantity));

            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                Glide.with(itemView.getContext()).load(item.imageUrl)
                        .centerCrop().placeholder(android.R.color.darker_gray).into(ivImage);
            } else {
                ivImage.setImageResource(android.R.color.darker_gray);
            }

            btnInc.setOnClickListener(v -> {
                CartManager.getInstance().addItem(
                        item.productId, item.name, item.price, item.imageUrl);
                item.quantity++;
                tvQty.setText(String.valueOf(item.quantity));
                if (listener != null) listener.onCartChanged();
            });

            btnDec.setOnClickListener(v -> {
                CartManager.getInstance().removeItem(item.productId);
                if (item.quantity > 1) {
                    item.quantity--;
                    tvQty.setText(String.valueOf(item.quantity));
                } else {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_ID) {
                        items.remove(pos);
                        notifyItemRemoved(pos);
                    }
                }
                if (listener != null) listener.onCartChanged();
            });
        }
    }
}
