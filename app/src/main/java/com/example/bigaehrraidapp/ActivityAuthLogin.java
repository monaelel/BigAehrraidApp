package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActivityAuthLogin extends AppCompatActivity {

    TextView tvGoToRegister;
    Button btnSignIn;
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_login);

        role = getIntent().getStringExtra("role");

        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        btnSignIn = findViewById(R.id.btnSignIn);

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAuthLogin.this, ActivityAuthRegister.class);
            intent.putExtra("role", role);
            startActivity(intent);
        });

        btnSignIn.setOnClickListener(v -> {
            // Sign in logic
            Intent intent;
            if ("restaurant".equals(role)) {
                intent = new Intent(ActivityAuthLogin.this, RestaurantMainActivity.class);
            } else {
                intent = new Intent(ActivityAuthLogin.this, CustomerMainActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}