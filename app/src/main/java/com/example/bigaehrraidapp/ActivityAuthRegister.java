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
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_register);

        role = getIntent().getStringExtra("role");

        rgUserRole = findViewById(R.id.rgUserRole);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Pre-select the radio button based on role passed in
        if ("restaurant".equals(role)) {
            rgUserRole.check(R.id.rbRestaurant);
        } else {
            rgUserRole.check(R.id.rbCustomer);
        }

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAuthRegister.this, ActivityAuthLogin.class);
            intent.putExtra("role", role);
            startActivity(intent);
            finish();
        });

        btnSignUp.setOnClickListener(v -> {
            int selectedId = rgUserRole.getCheckedRadioButtonId();
            Intent intent;
            if (selectedId == R.id.rbRestaurant) {
                intent = new Intent(ActivityAuthRegister.this, RestaurantMainActivity.class);
            } else {
                intent = new Intent(ActivityAuthRegister.this, CustomerMainActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}