package com.example.bigaehrraidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnOrderActionListener {
        void onAccept(int position);
        void onDecline(int position);
        void onReady(int position);
        void onOrderClick(int position);  // for order details (to be implemented later)
    }

    private final List<Order> orders;
    private final OnOrderActionListener listener;

    public OrderAdapter(List<Order> orders, OnOrderActionListener listener) {
        this.orders   = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Order order = orders.get(position);

        h.tvOrderId.setText("Order #" + order.orderId);
        h.tvCustomerName.setText(order.customerName);
        h.tvStatus.setText(order.status);
        h.tvItemCount.setText(order.itemCount + " items");
        h.tvTotalAmount.setText(String.format(Locale.getDefault(), "$ %.2f", order.totalAmount));

        // Hide all action views first
        h.layoutIncomingButtons.setVisibility(View.GONE);
        h.btnReady.setVisibility(View.GONE);

        switch (order.status) {
            case Order.STATUS_INCOMING:
                h.layoutIncomingButtons.setVisibility(View.VISIBLE);
                h.btnAccept.setOnClickListener(v -> listener.onAccept(h.getAdapterPosition()));
                h.btnDecline.setOnClickListener(v -> listener.onDecline(h.getAdapterPosition()));
                break;

            case Order.STATUS_PREPARING:
                h.btnReady.setVisibility(View.VISIBLE);
                h.btnReady.setOnClickListener(v -> listener.onReady(h.getAdapterPosition()));
                break;

            case Order.STATUS_COMPLETED:
                // No action buttons for completed orders
                break;
        }

        // Tap card to open order details (will be implemented on a separate branch)
        h.itemView.setOnClickListener(v -> listener.onOrderClick(h.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView      tvOrderId, tvCustomerName, tvStatus, tvItemCount, tvTotalAmount;
        LinearLayout  layoutIncomingButtons;
        Button        btnAccept;
        TextView      btnDecline, btnReady;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId             = itemView.findViewById(R.id.tvOrderId);
            tvCustomerName        = itemView.findViewById(R.id.tvCustomerName);
            tvStatus              = itemView.findViewById(R.id.tvStatus);
            tvItemCount           = itemView.findViewById(R.id.tvItemCount);
            tvTotalAmount         = itemView.findViewById(R.id.tvTotalAmount);
            layoutIncomingButtons = itemView.findViewById(R.id.layoutIncomingButtons);
            btnAccept             = itemView.findViewById(R.id.btnAccept);
            btnDecline            = itemView.findViewById(R.id.btnDecline);
            btnReady              = itemView.findViewById(R.id.btnReady);
        }
    }
}
