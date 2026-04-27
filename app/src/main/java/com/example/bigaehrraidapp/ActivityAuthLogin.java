package com.example.bigaehrraidapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class ActivityAuthLogin extends AppCompatActivity {

    private String   role;
    private EditText etEmail, etPassword;
    private Button   btnSignIn;
    private TextView tvGoToRegister;
    private CheckBox cbRememberMe;

    private AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth_login);

        // ── Role setup ───────────────────────────────────────────────
        role = getIntent().getStringExtra("role");
        if (role == null) role = "customer";

        authRepo = AuthRepository.getInstance(this);

        // ── View bindings ────────────────────────────────────────────
        etEmail        = findViewById(R.id.et_login_email);
        etPassword     = findViewById(R.id.etLoginPassword);
        btnSignIn      = findViewById(R.id.btnSignIn);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        cbRememberMe   = findViewById(R.id.cbRememberMe);

        ImageView btnBack         = findViewById(R.id.btnBack);
        TextView  tvForgotPassword = findViewById(R.id.tvForgotPassword);
        LinearLayout layoutCreateAccount = findViewById(R.id.layoutCreateAccount);
        TextView     tvContactAdmin      = findViewById(R.id.tvContactAdmin);

        // ── Restore preferences ──────────────────────────────────────
        cbRememberMe.setChecked(authRepo.isRemembered());

        // ── Back button ──────────────────────────────────────────────
        btnBack.setOnClickListener(v -> finish());

        // ── Forgot password ──────────────────────────────────────────
        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email to reset password",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            authRepo.resetPassword(email, new AuthRepository.AuthCallback() {
                @Override public void onSuccess() {
                    Toast.makeText(ActivityAuthLogin.this,
                            "Password reset email sent. Check your inbox.",
                            Toast.LENGTH_LONG).show();
                }
                @Override public void onFailure(String error) {
                    Toast.makeText(ActivityAuthLogin.this,
                            "Failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        // ── Role-based UI visibility ─────────────────────────────────
        layoutCreateAccount.setVisibility(View.VISIBLE);
        tvContactAdmin.setVisibility(View.GONE);

        // ── Navigation ───────────────────────────────────────────────
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityAuthRegister.class);
            intent.putExtra("role", role);
            startActivity(intent);
        });

        // ── Sign in ──────────────────────────────────────────────────
        btnSignIn.setOnClickListener(v -> performLogin());

        // ── Entrance animations ──────────────────────────────────────
        runEntranceAnimations();
    }

    /**
     * Validates fields and performs Firebase login with UI feedback.
     */
    private void performLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            // Bounce the sign-in button to draw attention
            bounceView(btnSignIn);
            return;
        }

        setLoginLoading(true);

        authRepo.login(email, password, role, cbRememberMe.isChecked(),
                new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                String cachedRole = authRepo.getCachedRole();
                Intent intent = "restaurant".equals(cachedRole)
                        ? new Intent(ActivityAuthLogin.this, RestaurantMainActivity.class)
                        : new Intent(ActivityAuthLogin.this, CustomerMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                setLoginLoading(false);
                Toast.makeText(ActivityAuthLogin.this,
                        "Login failed: " + error, Toast.LENGTH_LONG).show();
                // Shake the form card on failure
                shakeView(findViewById(R.id.cardLoginForm));
            }
        });
    }

    /**
     * Toggles the sign-in button between loading and normal state.
     */
    private void setLoginLoading(boolean loading) {
        btnSignIn.setEnabled(!loading);
        btnSignIn.setText(loading ? "Signing in…" : "Sign In");
        btnSignIn.setAlpha(loading ? 0.7f : 1f);
    }

    // ═══════════════════════════════════════════════════════════════
    //  ANTIGRAVITY ANIMATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Staggers entrance animations: logo floats up, card slides from bottom
     * with spring physics, and buttons fade in.
     */
    private void runEntranceAnimations() {
        View logo     = findViewById(R.id.ivLogo);
        View glow     = findViewById(R.id.viewLogoGlow);
        View title    = findViewById(R.id.tvAppTitle);
        View card     = findViewById(R.id.cardLoginForm);
        View links    = findViewById(R.id.layoutCreateAccount);

        // Start invisible & translated
        logo.setAlpha(0f);
        logo.setTranslationY(-30f);
        glow.setAlpha(0f);
        glow.setScaleX(0.5f);
        glow.setScaleY(0.5f);
        title.setAlpha(0f);
        title.setTranslationY(-20f);
        card.setAlpha(0f);
        card.setTranslationY(60f);
        links.setAlpha(0f);

        // Logo float-in with spring
        SpringAnimation logoSpring = new SpringAnimation(logo, DynamicAnimation.TRANSLATION_Y, 0f);
        logoSpring.getSpring()
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

        logo.animate().alpha(1f).setDuration(500).setStartDelay(100).start();
        logoSpring.setStartVelocity(-200f);
        logo.postDelayed(logoSpring::start, 100);

        // Glow pulse
        glow.animate().alpha(0.6f).scaleX(1f).scaleY(1f)
                .setDuration(800).setStartDelay(200).start();

        // Title slide
        title.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(300)
                .setInterpolator(new OvershootInterpolator(1.2f)).start();

        // Card spring up
        SpringAnimation cardSpring = new SpringAnimation(card, DynamicAnimation.TRANSLATION_Y, 0f);
        cardSpring.getSpring()
                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

        card.animate().alpha(1f).setDuration(400).setStartDelay(350).start();
        card.postDelayed(() -> cardSpring.setStartVelocity(-100f).start(), 350);

        // Links fade
        links.animate().alpha(1f).setDuration(400).setStartDelay(600).start();

        // Continuous subtle floating animation on logo
        startFloatingAnimation(logo);
    }

    /**
     * Perpetual gentle vertical float for the logo.
     */
    private void startFloatingAnimation(View view) {
        ObjectAnimator float1 = ObjectAnimator.ofFloat(view, "translationY", 0f, -8f);
        float1.setDuration(2000);
        ObjectAnimator float2 = ObjectAnimator.ofFloat(view, "translationY", -8f, 0f);
        float2.setDuration(2000);

        AnimatorSet floatSet = new AnimatorSet();
        floatSet.playSequentially(float1, float2);
        floatSet.setStartDelay(1200);
        floatSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                floatSet.start(); // loop forever
            }
        });
        floatSet.start();
    }

    /**
     * Quick spring bounce on a view (for validation feedback).
     */
    private void bounceView(View view) {
        SpringAnimation bounce = new SpringAnimation(view, DynamicAnimation.SCALE_X, 1f);
        bounce.getSpring()
                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        view.setScaleX(0.92f);
        view.setScaleY(0.92f);
        bounce.start();

        SpringAnimation bounceY = new SpringAnimation(view, DynamicAnimation.SCALE_Y, 1f);
        bounceY.getSpring()
                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        bounceY.start();
    }

    /**
     * Horizontal shake animation (for login failure feedback).
     */
    private void shakeView(View view) {
        SpringAnimation shake = new SpringAnimation(view, DynamicAnimation.TRANSLATION_X, 0f);
        shake.getSpring()
                .setStiffness(SpringForce.STIFFNESS_HIGH)
                .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
        view.setTranslationX(20f);
        shake.start();
    }
}
