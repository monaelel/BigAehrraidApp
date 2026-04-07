package com.example.bigaehrraidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ProductStockAdapter extends RecyclerView.Adapter<ProductStockAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onDelete(int position);
        void onEdit(int position);
    }

    private final List<ProductStock> products;
    private final OnItemActionListener listener;

    public ProductStockAdapter(List<ProductStock> products, OnItemActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductStock product = products.get(position);
        holder.tvProductName.setText(product.name);
        holder.tvStocks.setText("Stocks: " + product.stockCount + " units");
        holder.tvPrice.setText(String.format(Locale.getDefault(),
                "Price: $%.2f / unit", product.pricePerUnit));

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(holder.getAdapterPosition()));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvStocks, tvPrice, btnEdit;
        Button btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvStocks      = itemView.findViewById(R.id.tvStocks);
            tvPrice       = itemView.findViewById(R.id.tvPrice);
            btnDelete     = itemView.findViewById(R.id.btnDelete);
            btnEdit       = itemView.findViewById(R.id.btnEdit);
        }
    }
}
