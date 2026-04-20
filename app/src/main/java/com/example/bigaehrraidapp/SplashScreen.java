package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(() -> {
            AuthRepository auth = AuthRepository.getInstance(this);

            // If user didn't check "Remember Me", sign them out on every cold start
            if (auth.isLoggedIn() && !auth.isRemembered()) {
                auth.logout();
            }

            if (auth.isLoggedIn()) {
                String role = auth.getCachedRole();
                Intent intent;
                if ("restaurant".equals(role)) {
                    intent = new Intent(this, RestaurantMainActivity.class);
                } else if ("customer".equals(role)) {
                    intent = new Intent(this, CustomerMainActivity.class);
                } else {
                    intent = new Intent(this, RoleSelectionActivity.class);
                }
                startActivity(intent);
            } else {
                startActivity(new Intent(this, RoleSelectionActivity.class));
            }
            finish();
        }, SPLASH_TIME);
    }
}
