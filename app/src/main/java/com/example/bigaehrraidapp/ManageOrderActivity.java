package com.example.bigaehrraidapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageOrderActivity extends AppCompatActivity {

    RecyclerView rvOrders;
    List<Order> orders;
    OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_order);

        rvOrders = findViewById(R.id.rvOrders);

        // Sample orders — replace with real data source later
        orders = new ArrayList<>();
        orders.add(new Order("372873", "Alice Martin",   3, 47.50, Order.STATUS_INCOMING));
        orders.add(new Order("372874", "Bob Tremblay",   2, 23.00, Order.STATUS_INCOMING));
        orders.add(new Order("372875", "Claire Nguyen",  5, 88.75, Order.STATUS_PREPARING));
        orders.add(new Order("372876", "David Lavoie",   1, 12.99, Order.STATUS_PREPARING));
        orders.add(new Order("372877", "Emma Côté",      4, 61.20, Order.STATUS_COMPLETED));
        orders.add(new Order("372878", "Frank Gagnon",   2, 31.40, Order.STATUS_COMPLETED));

        adapter = new OrderAdapter(orders, new OrderAdapter.OnOrderActionListener() {

            @Override
            public void onAccept(int position) {
                orders.get(position).status = Order.STATUS_PREPARING;
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onDecline(int position) {
                orders.remove(position);
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onReady(int position) {
                orders.get(position).status = Order.STATUS_COMPLETED;
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onOrderClick(int position) {
                // TODO: navigate to OrderDetailActivity (separate branch)
                Toast.makeText(ManageOrderActivity.this,
                        "Order #" + orders.get(position).orderId + " details — coming soon",
                        Toast.LENGTH_SHORT).show();
            }
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
