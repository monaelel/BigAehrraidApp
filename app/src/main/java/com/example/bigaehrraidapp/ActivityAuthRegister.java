package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityAuthRegister extends AppCompatActivity {

    RadioGroup rgUserRole;
    Button     btnSignUp;
    TextView   tvGoToLogin;

    EditText etEmail, etPassword, etConfirmPassword;

    AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_register);

        authRepo = AuthRepository.getInstance(this);

        rgUserRole        = findViewById(R.id.rgUserRole);
        btnSignUp         = findViewById(R.id.btnSignUp);
        tvGoToLogin       = findViewById(R.id.tvGoToLogin);
        etEmail           = findViewById(R.id.etRegisterEmail);
        etPassword        = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, ActivityAuthLogin.class));
            finish();
        });

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

            int selectedId = rgUserRole.getCheckedRadioButtonId();
            String role;
            if (selectedId == R.id.rbRestaurant) {
                role = "restaurant";
            } else if (selectedId == R.id.rbCustomer) {
                role = "customer";
            } else {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSignUp.setEnabled(false);
            btnSignUp.setText("Creating account...");

            authRepo.register(email, password, role, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess() {
                    Intent intent;
                    if ("restaurant".equals(role)) {
                        intent = new Intent(ActivityAuthRegister.this, RestaurantMainActivity.class);
                    } else {
                        intent = new Intent(ActivityAuthRegister.this, CustomerMainActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Sign Up");
                    Toast.makeText(ActivityAuthRegister.this, "Register failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
