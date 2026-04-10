package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityAuthLogin extends AppCompatActivity {

    EditText      etEmail, etPassword;
    Button        btnSignIn;
    TextView      tvGoToRegister;
    String   role;
    EditText etEmail, etPassword;
    Button   btnSignIn;
    TextView tvGoToRegister;
    CheckBox cbRememberMe;

    AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_login);

        role    = getIntent().getStringExtra("role");
        authRepo = AuthRepository.getInstance(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etEmail        = findViewById(R.id.et_login_email);
        etPassword     = findViewById(R.id.etLoginPassword);
        btnSignIn      = findViewById(R.id.btnSignIn);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        cbRememberMe   = findViewById(R.id.cbRememberMe);

        // Restore saved preference
        cbRememberMe.setChecked(authRepo.isRemembered());

        // Show the right bottom section based on role
        LinearLayout layoutCreateAccount = findViewById(R.id.layoutCreateAccount);
        TextView     tvContactAdmin      = findViewById(R.id.tvContactAdmin);
        if ("restaurant".equals(role)) {
            tvContactAdmin.setVisibility(View.VISIBLE);
            layoutCreateAccount.setVisibility(View.GONE);
        } else {
            layoutCreateAccount.setVisibility(View.VISIBLE);
            tvContactAdmin.setVisibility(View.GONE);
        }

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAuthLogin.this, ActivityAuthRegister.class);
            intent.putExtra("role", role);
            startActivity(intent);
        });

        btnSignIn.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSignIn.setEnabled(false);
            btnSignIn.setText("Signing in...");

            boolean rememberMe = cbRememberMe.isChecked();
            authRepo.login(email, password, role, rememberMe, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess() {
                    String cachedRole = authRepo.getCachedRole();
                    Intent intent;
                    if ("restaurant".equals(cachedRole)) {
                        intent = new Intent(ActivityAuthLogin.this, RestaurantMainActivity.class);
                    } else {
                        intent = new Intent(ActivityAuthLogin.this, CustomerMainActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    btnSignIn.setEnabled(true);
                    btnSignIn.setText("Sign In");
                    Toast.makeText(ActivityAuthLogin.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
