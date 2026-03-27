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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_login);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        btnSignIn = findViewById(R.id.btnSignIn);

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAuthLogin.this, ActivityAuthRegister.class);
            startActivity(intent);
        });

        btnSignIn.setOnClickListener(v -> {
            // Sign in logic
        });
    }
}