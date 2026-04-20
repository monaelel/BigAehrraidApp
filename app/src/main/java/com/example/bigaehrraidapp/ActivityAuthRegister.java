package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityAuthRegister extends AppCompatActivity {

    String   role;
    Button   btnSignUp;
    TextView tvGoToLogin;
    EditText etEmail, etPassword, etConfirmPassword;

    AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_register);

        role     = getIntent().getStringExtra("role");
        authRepo = AuthRepository.getInstance(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSignUp         = findViewById(R.id.btnSignUp);
        tvGoToLogin       = findViewById(R.id.tvGoToLogin);
        etEmail           = findViewById(R.id.etRegisterEmail);
        etPassword        = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        // Restaurants cannot self-register
//        if ("restaurant".equals(role)) {
//            Toast.makeText(this,
//                "Restaurant accounts are managed by admin. Please sign in with your provided credentials.",
//                Toast.LENGTH_LONG).show();
//            goToLogin();
//            return;
//        }

        tvGoToLogin.setOnClickListener(v -> goToLogin());

        btnSignUp.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm  = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSignUp.setEnabled(false);
            btnSignUp.setText("Creating account...");

            authRepo.register(email, password, "customer", new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess() {
                    // Sign out so user must log in manually
                    authRepo.logout();

                    // Show dialog — navigate to login only after user taps OK
                    new AlertDialog.Builder(ActivityAuthRegister.this)
                        .setTitle("Account Created!")
                        .setMessage("Your account has been created successfully.\nPlease sign in to continue.")
                        .setCancelable(false)
                        .setPositiveButton("Sign In", (dialog, which) -> goToLogin())
                        .show();
                }

                @Override
                public void onFailure(String error) {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Sign Up");
                    Toast.makeText(ActivityAuthRegister.this,
                        "Registration failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, ActivityAuthLogin.class);
        intent.putExtra("role", "customer");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
