package com.example.bigaehrraidapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_ROLE   = "user_role";

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

    // ── Auth state ────────────────────────────────────────────────────────────

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

    public void logout() {
        auth.signOut();
        prefs.edit().remove(KEY_ROLE).apply();
    }

    // ── Register ──────────────────────────────────────────────────────────────

    public void register(String email, String password, String role, AuthCallback cb) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> {
                String uid = result.getUser().getUid();

                Map<String, Object> userDoc = new HashMap<>();
                userDoc.put("email", email);
                userDoc.put("role",  role);

                db.collection("users").document(uid).set(userDoc)
                  .addOnSuccessListener(v -> {
                      if ("restaurant".equals(role)) {
                          Map<String, Object> restaurantDoc = new HashMap<>();
                          restaurantDoc.put("email", email);
                          restaurantDoc.put("name",  "");
                          restaurantDoc.put("phone", "");

                          db.collection("restaurants").document(uid).set(restaurantDoc)
                            .addOnSuccessListener(v2 -> {
                                saveRole(role);
                                cb.onSuccess();
                            })
                            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
                      } else {
                          saveRole(role);
                          cb.onSuccess();
                      }
                  })
                  .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public void login(String email, String password, AuthCallback cb) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> {
                String uid = result.getUser().getUid();
                db.collection("users").document(uid).get()
                  .addOnSuccessListener(doc -> {
                      if (doc.exists()) {
                          String role = doc.getString("role");
                          saveRole(role);
                      }
                      cb.onSuccess();
                  })
                  .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void saveRole(String role) {
        prefs.edit().putString(KEY_ROLE, role).apply();
    }
}
