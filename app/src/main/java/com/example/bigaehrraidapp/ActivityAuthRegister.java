package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActivityAuthRegister extends AppCompatActivity {

    RadioGroup rgUserRole;
    Button btnSignUp;
    TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_register);
        rgUserRole = findViewById(R.id.rgUserRole);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAuthRegister.this, ActivityAuthLogin.class);
            startActivity(intent);
            finish(); // Optional: closes register so back button doesn't loop
        });

        btnSignUp.setOnClickListener(v -> {
            int selectedId = rgUserRole.getCheckedRadioButtonId();
            if (selectedId == R.id.rbCustomer) {
                // Customer logic
            } else if (selectedId == R.id.rbRestaurant) {
                // Restaurant logic
            }
        });
    }
}