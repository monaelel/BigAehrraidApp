package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    Button btnCustomer, btnRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_role_selection);

        btnCustomer = findViewById(R.id.btnCustomer);
        btnRestaurant = findViewById(R.id.btnRestaurant);

        btnCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, ActivityAuthLogin.class);
            intent.putExtra("role", "customer");
            startActivity(intent);
        });

        btnRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, ActivityAuthLogin.class);
            intent.putExtra("role", "restaurant");
            startActivity(intent);
        });
    }
}
