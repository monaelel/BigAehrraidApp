package com.example.bigaehrraidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private final List<OrderItem> items;

    public OrderItemAdapter(List<OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        OrderItem item = items.get(position);
        h.tvProductName.setText(item.name != null ? item.name : "");
        h.tvItemTotal.setText(String.format(Locale.getDefault(), "$ %.2f", item.price * item.quantity));
        h.tvQtyPrice.setText(String.format(Locale.getDefault(), "%d x $ %.2f", item.quantity, item.price));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvItemTotal, tvQtyPrice;

        ViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvItemTotal   = itemView.findViewById(R.id.tvItemTotal);
            tvQtyPrice    = itemView.findViewById(R.id.tvQtyPrice);
        }
    }
}
