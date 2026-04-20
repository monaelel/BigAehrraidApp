package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartManager.OnCartChangeListener {

    private RecyclerView rvCartItems;
    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();
    
    private TextView tvEmptyCart;
    private TextView tvCartSubtotal;
    private View layoutBottom;
    private Button btnProceedToCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvCartItems = findViewById(R.id.rvCartItems);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvCartSubtotal = findViewById(R.id.tvCartSubtotal);
        layoutBottom = findViewById(R.id.layoutBottom);
        btnProceedToCheckout = findViewById(R.id.btnProceedToCheckout);

        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemActionListener() {
            @Override
            public void onQuantityChanged(String productId, int newQuantity) {
                CartManager.getInstance().updateQuantity(productId, newQuantity);
            }

            @Override
            public void onRemoved(String productId) {
                CartManager.getInstance().removeItem(productId);
            }
        });
        rvCartItems.setAdapter(adapter);

        btnProceedToCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            startActivity(intent);
        });

        CartManager.getInstance().setListener(this);
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartManager.getInstance().setListener(null);
    }

    @Override
    public void onCartChanged() {
        updateUI();
    }

    private void updateUI() {
        cartItems.clear();
        cartItems.addAll(CartManager.getInstance().getItems());
        adapter.notifyDataSetChanged();

        if (cartItems.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            layoutBottom.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.GONE);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            layoutBottom.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.VISIBLE);

            double subtotal = CartManager.getInstance().getSubtotal();
            tvCartSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        }
    }
}
