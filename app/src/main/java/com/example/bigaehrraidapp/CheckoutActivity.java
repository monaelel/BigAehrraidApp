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
import com.stripe.android.Stripe;

public class CheckoutActivity extends AppCompatActivity {

    private CardInputWidget cardInputWidget;
    private Button btnPay;

    private Stripe stripe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        stripe = new Stripe(this, "pk_test_51TOFvo8atuREOtKl9JXwXAwP3zTenyXKPdVQkncs2vWWvqbFEoviLjKbT5hgjJsmnV5aa9N5JAjYrWUAbAqvxa5H00PrfzsJ3g");

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

        btnPay.setOnClickListener(v -> processPayment(total));
    }

    private void processPayment(double totalAmount) {
        com.stripe.android.model.PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();

        if (params == null) {
            Toast.makeText(this, "Please enter valid card details", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPay.setEnabled(false);
        btnPay.setText("Processing securely...");

        // 1. Call Firebase Function to get Client Secret
        int amountInCents = (int) Math.round(totalAmount * 100);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("amount", amountInCents);
        data.put("currency", "usd");

        com.google.firebase.functions.FirebaseFunctions.getInstance()
                .getHttpsCallable("createPaymentIntent")
                .call(data)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    java.util.Map<String, Object> result = (java.util.Map<String, Object>) task.getResult().getData();
                    String clientSecret = (String) result.get("clientSecret");

                    // 2. Confirm Payment with Stripe SDK
                    com.stripe.android.model.ConfirmPaymentIntentParams confirmParams = com.stripe.android.model.ConfirmPaymentIntentParams
                            .createWithPaymentMethodCreateParams(params, clientSecret);
                    
                    stripe.confirmPayment(this, confirmParams);
                    return null;
                })
                .addOnFailureListener(e -> {
                    btnPay.setEnabled(true);
                    double currentTotal = CartManager.getInstance().getSubtotal() * 1.13;
                    btnPay.setText(String.format(java.util.Locale.getDefault(), "Pay $%.2f", currentTotal));
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        stripe.onPaymentResult(requestCode, data, new com.stripe.android.ApiResultCallback<com.stripe.android.PaymentIntentResult>() {
            @Override
            public void onSuccess(com.stripe.android.PaymentIntentResult result) {
                com.stripe.android.model.StripeIntent intent = result.getIntent();
                if (intent.getStatus() == com.stripe.android.model.StripeIntent.Status.Succeeded) {
                    new AlertDialog.Builder(CheckoutActivity.this)
                        .setTitle("Payment Successful!")
                        .setMessage("Your order has been placed securely.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            CartManager.getInstance().clearCart();
                            finish();
                        })
                        .setCancelable(false)
                        .show();
                } else if (intent.getStatus() == com.stripe.android.model.StripeIntent.Status.RequiresPaymentMethod) {
                    btnPay.setEnabled(true);
                    btnPay.setText("Pay");
                    Toast.makeText(CheckoutActivity.this, "Payment failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                btnPay.setEnabled(true);
                btnPay.setText("Pay");
                Toast.makeText(CheckoutActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
