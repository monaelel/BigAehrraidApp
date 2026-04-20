package com.example.bigaehrraidapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private static final String PREFS_NAME        = "auth_prefs";
    private static final String KEY_ROLE          = "user_role";
    private static final String KEY_REMEMBER_ME   = "remember_me";

    private static AuthRepository instance;
    private final FirebaseAuth      auth;
    private final FirebaseFirestore db;
    private final SharedPreferences prefs;

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private AuthRepository(Context context) {
        auth  = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) instance = new AuthRepository(context);
        return instance;
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getCachedRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public boolean isRemembered() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public void setRememberMe(boolean remember) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, remember).apply();
    }

    public void logout() {
        auth.signOut();
        prefs.edit().remove(KEY_ROLE).remove(KEY_REMEMBER_ME).apply();
    }

    public void register(String email, String password, String role, AuthCallback cb) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> {
                String uid = result.getUser().getUid();

                // ── Notify the caller immediately — don't block on Firestore ──
                saveRole(role);
                cb.onSuccess();

                // ── Write Firestore documents in the background ───────────────
                Map<String, Object> userDoc = new HashMap<>();
                userDoc.put("email", email);
                userDoc.put("role",  role);
                db.collection("users").document(uid).set(userDoc);

                if ("restaurant".equals(role)) {
                    RestaurantSeeder.loadSeedForEmail(email, new RestaurantSeeder.SeedCallback() {
                        @Override
                        public void onFound(java.util.Map<String, Object> seedData) {
                            seedData.put("email", email);
                            db.collection("restaurants").document(uid).set(seedData);
                        }
                        @Override
                        public void onNotFound() {
                            Map<String, Object> restaurantDoc = new HashMap<>();
                            restaurantDoc.put("email", email);
                            restaurantDoc.put("name",  "");
                            restaurantDoc.put("phone", "");
                            restaurantDoc.put("mail", email); // Use email as default mail
                            restaurantDoc.put("neighborhood", "");
                            restaurantDoc.put("street", "");
                            restaurantDoc.put("city", "");
                            restaurantDoc.put("province", "");
                            restaurantDoc.put("postalCode", "");
                            restaurantDoc.put("lat", 0.0);
                            restaurantDoc.put("lng", 0.0);
                            db.collection("restaurants").document(uid).set(restaurantDoc);
                        }
                    });
                }
            })
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Logs in and validates that the user's stored role matches requiredRole.
     * Pass null for requiredRole to skip role validation.
     */
    public void login(String email, String password, String requiredRole,
                      boolean rememberMe, AuthCallback cb) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> {
                setRememberMe(rememberMe);
                String uid = result.getUser().getUid();
                db.collection("users").document(uid).get()
                  .addOnSuccessListener(doc -> {
                      String storedRole = doc.exists() ? doc.getString("role") : null;

                      // If Firestore returned nothing, fall back to cached role
                      if (storedRole == null) storedRole = prefs.getString(KEY_ROLE, requiredRole);

                      saveRole(storedRole);

                      if (requiredRole != null && !requiredRole.equals(storedRole)) {
                          auth.signOut();
                          prefs.edit().remove(KEY_ROLE).apply();
                          cb.onFailure("This account is not registered as a " + requiredRole + ".");
                          return;
                      }
                      cb.onSuccess();
                  })
                  .addOnFailureListener(e -> {
                      // Firestore unreachable (offline) — use cached role or trust requiredRole
                      String fallbackRole = prefs.getString(KEY_ROLE, requiredRole);
                      saveRole(fallbackRole);

                      if (requiredRole != null && !requiredRole.equals(fallbackRole)) {
                          auth.signOut();
                          prefs.edit().remove(KEY_ROLE).apply();
                          cb.onFailure("This account is not registered as a " + requiredRole + ".");
                          return;
                      }
                      cb.onSuccess();
                  });
            })
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    private void saveRole(String role) {
        prefs.edit().putString(KEY_ROLE, role).apply();
    }
}
