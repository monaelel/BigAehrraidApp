package com.example.bigaehrraidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckoutActivity extends AppCompatActivity {

    private static final double TAX_RATE = 0.10;

    private PaymentSheet   paymentSheet;
    private String         clientSecret;

    private CartAdapter    cartAdapter;
    private List<CartItem> cartItems;

    private TextView    tvRestaurantName, tvSubtotal, tvTax, tvTotal;
    private Button      btnPay;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Init Stripe
        PaymentConfiguration.init(getApplicationContext(),
                BuildConfig.STRIPE_PUBLISHABLE_KEY);

        // PaymentSheet must be created in onCreate
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // Views
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvSubtotal       = findViewById(R.id.tvSubtotal);
        tvTax            = findViewById(R.id.tvTax);
        tvTotal          = findViewById(R.id.tvTotal);
        btnPay           = findViewById(R.id.btnPay);
        progressBar      = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Cart
        CartManager cart = CartManager.getInstance();
        tvRestaurantName.setText(cart.getRestaurantName());

        cartItems = new ArrayList<>(cart.getItems());
        cartAdapter = new CartAdapter(cartItems, this::refreshTotals);

        RecyclerView rv = findViewById(R.id.rvCartItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(cartAdapter);

        refreshTotals();

        btnPay.setOnClickListener(v -> {
            if (CartManager.getInstance().isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            createPaymentIntent();
        });
    }

    // ── Totals ────────────────────────────────────────────────────────────────

    private void refreshTotals() {
        double subtotal = CartManager.getInstance().getSubtotal();
        double tax      = subtotal * TAX_RATE;
        double total    = subtotal + tax;

        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        tvTax.setText(String.format(Locale.getDefault(), "$%.2f", tax));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
        btnPay.setText(String.format(Locale.getDefault(), "Pay $%.2f", total));

        boolean empty = CartManager.getInstance().isEmpty();
        btnPay.setEnabled(!empty);
        btnPay.setAlpha(empty ? 0.5f : 1f);
    }

    // ── Stripe: Create PaymentIntent ──────────────────────────────────────────

    private void createPaymentIntent() {
        setLoading(true);

        double subtotal    = CartManager.getInstance().getSubtotal();
        long   amountCents = Math.round((subtotal + subtotal * TAX_RATE) * 100);

        OkHttpClient client = new OkHttpClient();

        okhttp3.RequestBody body = new FormBody.Builder()
                .add("amount",   String.valueOf(amountCents))
                .add("currency", "usd")
                .add("payment_method_types[]", "card")
                .build();

        Request request = new Request.Builder()
                .url("https://api.stripe.com/v1/payment_intents")
                .addHeader("Authorization", "Bearer " + BuildConfig.STRIPE_SECRET_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Call call,
                                  @androidx.annotation.NonNull IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(CheckoutActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@androidx.annotation.NonNull Call call,
                                   @androidx.annotation.NonNull Response response)
                    throws IOException {
                String responseBody = response.body() != null
                        ? response.body().string() : "";
                runOnUiThread(() -> {
                    setLoading(false);
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (json.has("error")) {
                            String msg = json.getJSONObject("error")
                                    .optString("message", "Unknown error");
                            Toast.makeText(CheckoutActivity.this,
                                    "Payment setup failed: " + msg,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        clientSecret = json.getString("client_secret");
                        presentPaymentSheet();
                    } catch (Exception e) {
                        Toast.makeText(CheckoutActivity.this,
                                "Payment setup failed. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // ── Stripe: Present PaymentSheet ──────────────────────────────────────────

    private void presentPaymentSheet() {
        PaymentSheet.Configuration config =
                new PaymentSheet.Configuration.Builder("BigAehrraidApp").build();
        paymentSheet.presentWithPaymentIntent(clientSecret, config);
    }

    // ── Stripe: Handle result ─────────────────────────────────────────────────

    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            placeOrder();
        } else if (result instanceof PaymentSheetResult.Failed) {
            String msg = ((PaymentSheetResult.Failed) result)
                    .getError().getLocalizedMessage();
            Toast.makeText(this, "Payment failed: " + msg, Toast.LENGTH_LONG).show();
        }
        // PaymentSheetResult.Canceled → user dismissed; do nothing
    }

    // ── Save order to Firestore ───────────────────────────────────────────────

    private void placeOrder() {
        setLoading(true);

        CartManager      cart = CartManager.getInstance();
        OrderRepository  repo = OrderRepository.getInstance(
                AuthRepository.getInstance(this));

        double subtotal = cart.getSubtotal();
        double taxes    = subtotal * TAX_RATE;

        repo.placeOrder(
                cart.getRestaurantId(),
                cart.getItems(),
                subtotal,
                taxes,
                new OrderRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        cart.clear();
                        setLoading(false);
                        new AlertDialog.Builder(CheckoutActivity.this)
                                .setTitle("Order placed!")
                                .setMessage("Payment successful. The restaurant has been notified.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (d, w) -> finish())
                                .show();
                    }

                    @Override
                    public void onFailure(String error) {
                        cart.clear();
                        setLoading(false);
                        // Payment succeeded — alert with warning
                        new AlertDialog.Builder(CheckoutActivity.this)
                                .setTitle("Order recorded with a warning")
                                .setMessage("Payment succeeded but we could not save the order: "
                                        + error + "\nPlease contact support.")
                                .setPositiveButton("OK", (d, w) -> finish())
                                .show();
                    }
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnPay.setEnabled(!loading);
    }
}
