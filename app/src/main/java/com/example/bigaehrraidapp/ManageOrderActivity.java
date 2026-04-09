package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageOrderActivity extends AppCompatActivity {

    RecyclerView    rvOrders;
    List<Order>     orders;
    OrderAdapter    adapter;
    OrderRepository orderRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_order);

        ((ImageView) findViewById(R.id.btnBack)).setOnClickListener(v -> finish());

        rvOrders  = findViewById(R.id.rvOrders);
        orders    = new ArrayList<>();

        AuthRepository authRepo = AuthRepository.getInstance(this);
        orderRepo = OrderRepository.getInstance(authRepo);

        adapter = new OrderAdapter(orders, new OrderAdapter.OnOrderActionListener() {

            @Override
            public void onAccept(int position) {
                Order o = orders.get(position);
                orderRepo.updateOrderStatus(o.orderId, Order.STATUS_PREPARING,
                    new OrderRepository.ActionCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String e) {
                            Toast.makeText(ManageOrderActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    });
            }

            @Override
            public void onDecline(int position) {
                Order o = orders.get(position);
                orderRepo.updateOrderStatus(o.orderId, Order.STATUS_DECLINED,
                    new OrderRepository.ActionCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String e) {
                            Toast.makeText(ManageOrderActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    });
            }

            @Override
            public void onReady(int position) {
                Order o = orders.get(position);
                orderRepo.updateOrderStatus(o.orderId, Order.STATUS_COMPLETED,
                    new OrderRepository.ActionCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String e) {
                            Toast.makeText(ManageOrderActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    });
            }

            @Override
            public void onOrderClick(int position) {
                Intent intent = new Intent(ManageOrderActivity.this, OrderDetailActivity.class);
                intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, orders.get(position).orderId);
                startActivity(intent);
            }
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);

        orderRepo.listenToOrders(new OrderRepository.OrdersCallback() {
            @Override
            public void onOrdersUpdated(List<Order> updated) {
                orders.clear();
                orders.addAll(updated);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(ManageOrderActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        orderRepo.removeListener();
    }
}
