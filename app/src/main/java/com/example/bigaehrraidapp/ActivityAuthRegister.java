package com.example.bigaehrraidapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import java.util.HashMap;
import java.util.Map;

public class ActivityAuthRegister extends AppCompatActivity {

    private String   role;
    private Button   btnSignUp;
    private TextView tvGoToLogin;
    private EditText etEmail, etPassword, etConfirmPassword;
    private EditText etDescription, etOpeningHours;

    private AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_register);

        role     = getIntent().getStringExtra("role");
        authRepo = AuthRepository.getInstance(this);

        // ── View bindings ────────────────────────────────────────────
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSignUp         = findViewById(R.id.btnSignUp);
        tvGoToLogin       = findViewById(R.id.tvGoToLogin);
        etEmail           = findViewById(R.id.etRegisterEmail);
        etPassword        = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etDescription     = findViewById(R.id.et_register_description);
        etOpeningHours    = findViewById(R.id.et_register_opening_hours);

        if ("restaurant".equals(role)) {
            etDescription.setVisibility(View.VISIBLE);
            etOpeningHours.setVisibility(View.VISIBLE);
        }

        tvGoToLogin.setOnClickListener(v -> goToLogin());
        btnSignUp.setOnClickListener(v -> performRegistration());

        // ── Entrance animations ──────────────────────────────────────
        runEntranceAnimations();
    }

    /**
     * Validates all fields and delegates to the appropriate registration path.
     */
    private void performRegistration() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            bounceView(btnSignUp);
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            shakeView(findViewById(R.id.cardRegisterForm));
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("restaurant".equals(role)) {
            String description  = etDescription.getText().toString().trim();
            String openingHours = etOpeningHours.getText().toString().trim();
            if (description.isEmpty() || openingHours.isEmpty()) {
                Toast.makeText(this, "Please provide description and opening hours",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            registerRestaurant(email, password, description, openingHours);
        } else {
            registerCustomer(email, password);
        }
    }

    private void registerCustomer(String email, String password) {
        setRegisterLoading(true);
        authRepo.register(email, password, "customer", new AuthRepository.AuthCallback() {
            @Override public void onSuccess() { handleRegisterSuccess(); }
            @Override public void onFailure(String error) { handleRegisterFailure(error); }
        });
    }

    private void registerRestaurant(String email, String password,
                                     String description, String openingHours) {
        setRegisterLoading(true);

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("description", description);
        profileData.put("opening_hours", openingHours);
        profileData.put("name", "");
        profileData.put("phone", "");
        profileData.put("mail", email);
        profileData.put("neighborhood", "");
        profileData.put("street", "");
        profileData.put("city", "");
        profileData.put("province", "");
        profileData.put("postalCode", "");
        profileData.put("lat", 0.0);
        profileData.put("lng", 0.0);

        authRepo.registerRestaurant(email, password, profileData,
                new AuthRepository.AuthCallback() {
            @Override public void onSuccess() { handleRegisterSuccess(); }
            @Override public void onFailure(String error) { handleRegisterFailure(error); }
        });
    }

    private void handleRegisterSuccess() {
        authRepo.logout();
        new AlertDialog.Builder(this)
            .setTitle("Account Created!")
            .setMessage("Your account has been created successfully.\nPlease sign in to continue.")
            .setCancelable(false)
            .setPositiveButton("Sign In", (dialog, which) -> goToLogin())
            .show();
    }

    private void handleRegisterFailure(String error) {
        setRegisterLoading(false);
        Toast.makeText(this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
        shakeView(findViewById(R.id.cardRegisterForm));
    }

    private void setRegisterLoading(boolean loading) {
        btnSignUp.setEnabled(!loading);
        btnSignUp.setText(loading ? "Creating account…" : "Sign Up");
        btnSignUp.setAlpha(loading ? 0.7f : 1f);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, ActivityAuthLogin.class);
        intent.putExtra("role", role != null ? role : "customer");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // ═══════════════════════════════════════════════════════════════
    //  ANTIGRAVITY ANIMATIONS
    // ═══════════════════════════════════════════════════════════════

    private void runEntranceAnimations() {
        View logo  = findViewById(R.id.ivLogo);
        View card  = findViewById(R.id.cardRegisterForm);

        logo.setAlpha(0f);
        logo.setTranslationY(-30f);
        card.setAlpha(0f);
        card.setTranslationY(50f);

        // Logo float-in with spring
        SpringAnimation logoSpring = new SpringAnimation(logo, DynamicAnimation.TRANSLATION_Y, 0f);
        logoSpring.getSpring()
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        logo.animate().alpha(1f).setDuration(500).setStartDelay(100).start();
        logo.postDelayed(() -> {
            logoSpring.setStartVelocity(-200f);
            logoSpring.start();
        }, 100);

        // Card spring up
        SpringAnimation cardSpring = new SpringAnimation(card, DynamicAnimation.TRANSLATION_Y, 0f);
        cardSpring.getSpring()
                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        card.animate().alpha(1f).setDuration(400).setStartDelay(300).start();
        card.postDelayed(() -> {
            cardSpring.setStartVelocity(-100f);
            cardSpring.start();
        }, 300);

        // Floating animation on logo
        startFloatingAnimation(logo);
    }

    private void startFloatingAnimation(View view) {
        ObjectAnimator up = ObjectAnimator.ofFloat(view, "translationY", 0f, -6f);
        up.setDuration(1800);
        ObjectAnimator down = ObjectAnimator.ofFloat(view, "translationY", -6f, 0f);
        down.setDuration(1800);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(up, down);
        set.setStartDelay(1200);
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                set.start();
            }
        });
        set.start();
    }

    private void bounceView(View view) {
        SpringAnimation bx = new SpringAnimation(view, DynamicAnimation.SCALE_X, 1f);
        bx.getSpring().setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        SpringAnimation by = new SpringAnimation(view, DynamicAnimation.SCALE_Y, 1f);
        by.getSpring().setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        view.setScaleX(0.92f);
        view.setScaleY(0.92f);
        bx.start();
        by.start();
    }

    private void shakeView(View view) {
        SpringAnimation shake = new SpringAnimation(view, DynamicAnimation.TRANSLATION_X, 0f);
        shake.getSpring().setStiffness(SpringForce.STIFFNESS_HIGH)
                .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
        view.setTranslationX(20f);
        shake.start();
    }
}
