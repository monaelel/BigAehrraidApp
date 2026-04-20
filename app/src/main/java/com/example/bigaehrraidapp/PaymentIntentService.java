package com.example.bigaehrraidapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Creates a Stripe PaymentIntent by calling the Stripe API directly.
 *
 * ⚠️  FOR TEST / PROTOTYPE USE ONLY.
 *     In production, move this call to your own backend server so the
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
     * Calls back on the main thread.
     */
    public static void createPaymentIntent(long amountCents, String currency, Callback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler         handler  = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("amount",                    String.valueOf(amountCents))
                    .add("currency",                  currency)
                    .add("automatic_payment_methods[enabled]", "true")
                    .build();

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + BuildConfig.STRIPE_SECRET_KEY)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Stripe response (" + response.code() + "): " + responseBody);

                if (!response.isSuccessful()) {
                    JSONObject err = new JSONObject(responseBody);
                    String msg = err.optJSONObject("error") != null
                            ? err.getJSONObject("error").optString("message", "Unknown error")
                            : "HTTP " + response.code();
                    handler.post(() -> callback.onError(msg));
                    return;
                }

                JSONObject json         = new JSONObject(responseBody);
                String     clientSecret = json.getString("client_secret");
                handler.post(() -> callback.onSuccess(clientSecret));

            } catch (IOException | org.json.JSONException e) {
                Log.e(TAG, "createPaymentIntent failed", e);
                handler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
}
