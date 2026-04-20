package com.example.bigaehrraidapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.stripe.android.model.CardParams;
import com.stripe.android.view.CardInputWidget;

public class CheckoutActivity extends AppCompatActivity {

    private CardInputWidget cardInputWidget;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        double subtotal = CartManager.getInstance().getSubtotal();
        double taxes = subtotal * 0.13; // Example tax rate
        double total = subtotal + taxes;

        TextView tvSubtotal = findViewById(R.id.tvSubtotal);
        TextView tvTaxes = findViewById(R.id.tvTaxes);
        TextView tvTotal = findViewById(R.id.tvTotal);
        Button btnPay = findViewById(R.id.btnPay);

        tvSubtotal.setText(String.format(java.util.Locale.getDefault(), "$%.2f", subtotal));
        tvTaxes.setText(String.format(java.util.Locale.getDefault(), "$%.2f", taxes));
        tvTotal.setText(String.format(java.util.Locale.getDefault(), "$%.2f", total));
        btnPay.setText(String.format(java.util.Locale.getDefault(), "Pay $%.2f", total));

        cardInputWidget = findViewById(R.id.cardInputWidget);
        this.btnPay = btnPay;

        btnPay.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        CardParams cardParams = cardInputWidget.getCardParams();

        if (cardParams == null) {
            Toast.makeText(this, "Please enter valid card details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate payment processing delay
        btnPay.setEnabled(false);
        btnPay.setText("Processing...");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Mock Success!
            new AlertDialog.Builder(this)
                .setTitle("Payment Successful!")
                .setMessage("Your order has been placed securely.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate back or to order tracking
                    finish();
                })
                .setCancelable(false)
                .show();

            btnPay.setEnabled(true);
            double currentTotal = CartManager.getInstance().getSubtotal() * 1.13;
            btnPay.setText(String.format(java.util.Locale.getDefault(), "Pay $%.2f", currentTotal));

        }, 2000); // 2 second delay
    }
}
