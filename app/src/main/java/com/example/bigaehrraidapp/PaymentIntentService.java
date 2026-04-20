package com.example.bigaehrraidapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Creates a Stripe PaymentIntent by calling the Stripe REST API directly.
 *
 * Uses plain HttpURLConnection — no extra library required.
 *
 * ⚠️  FOR TEST / PROTOTYPE USE ONLY.
 *     In production, move this call to a backend server so the
 *     secret key is never shipped inside the APK.
 */
public class PaymentIntentService {

    private static final String TAG     = "PaymentIntentService";
    private static final String API_URL = "https://api.stripe.com/v1/payment_intents";

    public interface Callback {
        void onSuccess(String clientSecret);
        void onError(String message);
    }

    /**
     * Creates a PaymentIntent for the given amount (in cents) and currency.
     * Result is delivered on the main thread.
     */
    public static void createPaymentIntent(long amountCents, String currency, Callback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler         handler  = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(API_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization",  "Bearer " + BuildConfig.STRIPE_SECRET_KEY);
                conn.setRequestProperty("Content-Type",   "application/x-www-form-urlencoded");
                conn.setConnectTimeout(15_000);
                conn.setReadTimeout(15_000);
                conn.setDoOutput(true);

                String body = "amount=" + amountCents
                        + "&currency=" + currency
                        + "&automatic_payment_methods%5Benabled%5D=true";

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                InputStream is = code < 400 ? conn.getInputStream() : conn.getErrorStream();

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }
                String responseBody = sb.toString();
                Log.d(TAG, "Stripe response (" + code + "): " + responseBody);

                JSONObject json = new JSONObject(responseBody);

                if (code >= 400) {
                    String msg = "HTTP " + code;
                    if (json.has("error")) {
                        msg = json.getJSONObject("error").optString("message", msg);
                    }
                    final String finalMsg = msg;
                    handler.post(() -> callback.onError(finalMsg));
                    return;
                }

                String clientSecret = json.getString("client_secret");
                handler.post(() -> callback.onSuccess(clientSecret));

            } catch (Exception e) {
                Log.e(TAG, "createPaymentIntent failed", e);
                handler.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "Network error"));
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }
}
