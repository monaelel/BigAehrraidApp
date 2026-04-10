package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";

    TextView      tvOrderId, tvStatus, tvDateTime;
    TextView      tvCustomerName, tvCustomerPhone, tvCustomerAddress;
    TextView      tvItemsHeader;
    RecyclerView  rvItems;
    TextView      tvSubtotal, tvTaxes, tvTotal;
    Button        btnReady;
    ImageView     btnBack;
    ProgressBar   progressBar;

    OrderRepository   orderRepo;
    OrderItemAdapter  itemAdapter;
    List<OrderItem>   items = new ArrayList<>();

    String orderId;
    Order  currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId == null) { finish(); return; }

        AuthRepository authRepo = AuthRepository.getInstance(this);
        orderRepo = OrderRepository.getInstance(authRepo);

        tvOrderId         = findViewById(R.id.tvDetailOrderId);
        tvStatus          = findViewById(R.id.tvDetailStatus);
        tvDateTime        = findViewById(R.id.tvDetailDateTime);
        tvCustomerName    = findViewById(R.id.tvDetailCustomerName);
        tvCustomerPhone   = findViewById(R.id.tvDetailCustomerPhone);
        tvCustomerAddress = findViewById(R.id.tvDetailCustomerAddress);
        tvItemsHeader     = findViewById(R.id.tvDetailItemsHeader);
        rvItems           = findViewById(R.id.rvDetailItems);
        tvSubtotal        = findViewById(R.id.tvDetailSubtotal);
        tvTaxes           = findViewById(R.id.tvDetailTaxes);
        tvTotal           = findViewById(R.id.tvDetailTotal);
        btnReady          = findViewById(R.id.btnDetailReady);
        btnBack           = findViewById(R.id.btnDetailBack);
        progressBar       = findViewById(R.id.progressBarDetail);

        itemAdapter = new OrderItemAdapter(items);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);
        rvItems.setNestedScrollingEnabled(false);

        btnBack.setOnClickListener(v -> finish());

        btnReady.setOnClickListener(v -> {
            if (currentOrder == null) return;
            btnReady.setEnabled(false);
            orderRepo.updateOrderStatus(orderId, Order.STATUS_COMPLETED,
                new OrderRepository.ActionCallback() {
                    @Override public void onSuccess() {
                        Toast.makeText(OrderDetailActivity.this, "Order marked as ready", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override public void onFailure(String error) {
                        btnReady.setEnabled(true);
                        Toast.makeText(OrderDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
        });

        loadOrderDetail();
    }

    private void loadOrderDetail() {
        progressBar.setVisibility(View.VISIBLE);
        orderRepo.fetchOrderDetail(orderId, new OrderRepository.OrderDetailCallback() {
            @Override
            public void onSuccess(Order order, List<OrderItem> orderItems) {
                progressBar.setVisibility(View.GONE);
                currentOrder = order;
                populateUI(order, orderItems);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderDetailActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void populateUI(Order order, List<OrderItem> orderItems) {
        tvOrderId.setText("Order #" + order.orderId);
        tvStatus.setText(order.status != null ? order.status : "");

        if (order.createdAt > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault());
            tvDateTime.setText(sdf.format(new Date(order.createdAt)));
        } else {
            tvDateTime.setText("");
        }

        tvCustomerName.setText(order.customerName != null ? order.customerName : "—");
        tvCustomerPhone.setText(order.customerPhone != null ? order.customerPhone : "—");
        tvCustomerAddress.setText(order.customerAddress != null ? order.customerAddress : "—");

        items.clear();
        items.addAll(orderItems);
        itemAdapter.notifyDataSetChanged();
        tvItemsHeader.setText("Items (" + orderItems.size() + ")");

        double subtotal = 0;
        for (OrderItem item : orderItems) {
            subtotal += item.price * item.quantity;
        }
        double taxes = order.taxes > 0 ? order.taxes : subtotal * 0.08;
        double total = subtotal + taxes;

        tvSubtotal.setText(String.format(Locale.getDefault(), "$ %.2f", subtotal));
        tvTaxes.setText(String.format(Locale.getDefault(), "$ %.2f", taxes));
        tvTotal.setText(String.format(Locale.getDefault(), "$ %.2f", total));

        if (Order.STATUS_PREPARING.equals(order.status)) {
            btnReady.setVisibility(View.VISIBLE);
        } else {
            btnReady.setVisibility(View.GONE);
        }
    }
}
