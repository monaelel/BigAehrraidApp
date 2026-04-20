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

import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private CartManager   cart;
    private CartAdapter   adapter;
    private List<CartItem> cartItems;

    private TextView    tvSubtotal, tvTax, tvTotal;
    private Button      btnPay;
    private ProgressBar progressBar;

    private PaymentSheet paymentSheet;
    private String       clientSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Init Stripe
        PaymentConfiguration.init(getApplicationContext(), BuildConfig.STRIPE_PUBLISHABLE_KEY);

        // PaymentSheet must be created in onCreate
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Views
        tvSubtotal  = findViewById(R.id.tvSubtotal);
        tvTax       = findViewById(R.id.tvTax);
        tvTotal     = findViewById(R.id.tvTotal);
        btnPay      = findViewById(R.id.btnPay);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Cart
        cart      = CartManager.getInstance();
        cartItems = cart.getItems();

        RecyclerView rv = findViewById(R.id.rvCart);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cartItems, new CartAdapter.OnQuantityChanged() {
            @Override
            public void onIncrement(String productId) {
                // find the CartItem and add 1
                for (CartItem ci : cartItems) {
                    if (ci.productId.equals(productId)) {
                        cart.addItem(new CartItem(ci.productId, ci.name, ci.imageUrl, ci.price));
                        break;
                    }
                }
                refreshCart();
            }

            @Override
            public void onDecrement(String productId) {
                cart.removeItem(productId);
                refreshCart();
            }
        });
        rv.setAdapter(adapter);

        updateTotals();

        btnPay.setOnClickListener(v -> startPayment());
    }

    // ── Payment flow ──────────────────────────────────────────────────────────

    private void startPayment() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        long amountCents = Math.round(cart.getTotal() * 100);

        PaymentIntentService.createPaymentIntent(amountCents, "usd", new PaymentIntentService.Callback() {
            @Override
            public void onSuccess(String secret) {
                clientSecret = secret;
                setLoading(false);
                presentPaymentSheet();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(CheckoutActivity.this,
                        "Could not start payment: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void presentPaymentSheet() {
        PaymentSheet.Configuration config = new PaymentSheet.Configuration.Builder("BigAehrraidApp")
                .build();
        paymentSheet.presentWithPaymentIntent(clientSecret, config);
    }

    private void onPaymentResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            saveOrderToFirestore();
        } else if (result instanceof PaymentSheetResult.Failed) {
            String msg = ((PaymentSheetResult.Failed) result).getError().getLocalizedMessage();
            Toast.makeText(this, "Payment failed: " + msg, Toast.LENGTH_LONG).show();
        }
        // PaymentSheetResult.Canceled → user dismissed; do nothing
    }

    // ── Save order to Firestore after successful payment ──────────────────────

    private void saveOrderToFirestore() {
        setLoading(true);
        OrderRepository repo = OrderRepository.getInstance(AuthRepository.getInstance(this));

        repo.placeOrder(
                cart.getRestaurantId(),
                cart.getItems(),
                cart.getSubtotal(),
                cart.getTaxes(),
                new OrderRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        cart.clearCart();
                        setLoading(false);
                        showSuccess();
                    }

                    @Override
                    public void onFailure(String error) {
                        setLoading(false);
                        // Payment already succeeded – still clear cart & inform user
                        cart.clearCart();
                        new AlertDialog.Builder(CheckoutActivity.this)
                                .setTitle("Order recorded with a warning")
                                .setMessage("Payment succeeded but we could not save the order details: "
                                        + error + "\nPlease contact support.")
                                .setPositiveButton("OK", (d, w) -> finish())
                                .show();
                    }
                });
    }

    private void showSuccess() {
        new AlertDialog.Builder(this)
                .setTitle("Order placed!")
                .setMessage("Your payment was successful. The restaurant has been notified.")
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void refreshCart() {
        cartItems.clear();
        cartItems.addAll(cart.getItems());
        adapter.notifyDataSetChanged();
        updateTotals();
    }

    private void updateTotals() {
        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", cart.getSubtotal()));
        tvTax.setText(String.format(Locale.getDefault(), "$%.2f", cart.getTaxes()));
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", cart.getTotal()));

        boolean empty = cart.isEmpty();
        btnPay.setEnabled(!empty);
        btnPay.setAlpha(empty ? 0.5f : 1f);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnPay.setEnabled(!loading);
    }
}
