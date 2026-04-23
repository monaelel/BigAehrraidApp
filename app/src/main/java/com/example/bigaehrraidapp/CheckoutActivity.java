package com.example.bigaehrraidapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

import com.stripe.android.model.CardParams;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.Stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        AuthRepository authRepo = AuthRepository.getInstance(this);
        if (!authRepo.isLoggedIn()) {
            Toast.makeText(this, "You must be logged in to place an order.", Toast.LENGTH_LONG).show();
            return;
        }

        com.stripe.android.model.PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();

        if (params == null) {
            Toast.makeText(this, "Please enter valid card details", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPay.setEnabled(false);
        btnPay.setText("Processing securely...");

        // 1. Call Firebase Function URL directly to avoid UNAUTHENTICATED errors
        int amountInCents = (int) Math.round(totalAmount * 100);
        String url = "https://us-central1-big-aehrraid.cloudfunctions.net/createPaymentIntent";

        String json = "{\"data\": {\"amount\": " + amountInCents + ", \"currency\": \"usd\"}}";
        RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    btnPay.setEnabled(true);
                    btnPay.setText("Retry Payment");
                    Toast.makeText(CheckoutActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                String responseBody = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String clientSecret = jsonResponse.getJSONObject("data").getString("clientSecret");

                    runOnUiThread(() -> {
                        com.stripe.android.model.ConfirmPaymentIntentParams confirmParams = com.stripe.android.model.ConfirmPaymentIntentParams
                                .createWithPaymentMethodCreateParams(params, clientSecret);
                        stripe.confirmPayment(CheckoutActivity.this, confirmParams);
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        btnPay.setEnabled(true);
                        btnPay.setText("Retry Payment");
                        Toast.makeText(CheckoutActivity.this, "Error parsing response: " + responseBody, Toast.LENGTH_SHORT).show();
                    });
                }
            }
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
                    saveOrderToDatabase();
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

    private void saveOrderToDatabase() {
        btnPay.setText("Finalizing order...");
        
        CartManager cart = CartManager.getInstance();
        Order order = new Order();
        order.restaurantId = cart.getRestaurantId();
        order.subtotal = cart.getSubtotal();
        order.taxes = order.subtotal * 0.13;
        order.totalAmount = order.subtotal + order.taxes;
        order.itemCount = cart.getTotalItemCount();
        order.status = Order.STATUS_INCOMING;
        
        // Retrieve actual user name from Auth
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        order.customerName = (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) 
                ? user.getDisplayName() : "Guest Customer";
        
        List<CartItem> items = CartManager.getInstance().getItems();
        
        OrderRepository.getInstance(AuthRepository.getInstance(this))
            .createOrder(order, items, new OrderRepository.ActionCallback() {
                @Override
                public void onSuccess() {
                    new AlertDialog.Builder(CheckoutActivity.this)
                        .setTitle("Payment Successful!")
                        .setMessage("Your order has been placed securely.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            CartManager.getInstance().clearCart();
                            finish();
                        })
                        .setCancelable(false)
                        .show();
                }

                @Override
                public void onFailure(String error) {
                    btnPay.setEnabled(true);
                    btnPay.setText("Try Again");
                    Toast.makeText(CheckoutActivity.this, "Payment succeeded but order failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
    }
}
