package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityAuthLogin extends AppCompatActivity {

    EditText      etEmail, etPassword;
    Button        btnSignIn;
    TextView      tvGoToRegister;
    ProgressBar   progressBar;

    AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_login);

        authRepo = AuthRepository.getInstance(this);

        etEmail        = findViewById(R.id.et_login_email);
        etPassword     = findViewById(R.id.etLoginPassword);
        btnSignIn      = findViewById(R.id.btnSignIn);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, ActivityAuthRegister.class))
        );

        btnSignIn.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSignIn.setEnabled(false);
            btnSignIn.setText("Signing in...");

            authRepo.login(email, password, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess() {
                    String role = authRepo.getCachedRole();
                    Intent intent;
                    if ("restaurant".equals(role)) {
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
