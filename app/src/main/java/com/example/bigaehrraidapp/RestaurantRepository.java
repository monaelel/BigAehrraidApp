package com.example.bigaehrraidapp;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RestaurantRepository {

    private static RestaurantRepository instance;
    private final FirebaseFirestore db;
    private final AuthRepository    authRepo;

    public interface Callback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    private RestaurantRepository(AuthRepository authRepo) {
        this.db       = FirebaseFirestore.getInstance();
        this.authRepo = authRepo;
    }

    public static synchronized RestaurantRepository getInstance(AuthRepository authRepo) {
        if (instance == null) instance = new RestaurantRepository(authRepo);
        return instance;
    }

    private String uid() { return authRepo.getCurrentUserId(); }

    public void loadProfile(Callback<Map<String, Object>> cb) {
        db.collection("restaurants").document(uid()).get()
          .addOnSuccessListener(doc -> {
              if (doc.exists()) cb.onSuccess(doc.getData());
              else              cb.onSuccess(new HashMap<>());
          })
          .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void saveProfile(Map<String, Object> data, Callback<Void> cb) {
        db.collection("restaurants").document(uid())
          .update(data)
          .addOnSuccessListener(v  -> cb.onSuccess(null))
          .addOnFailureListener(e  -> cb.onFailure(e.getMessage()));
    }

    @SuppressWarnings("unchecked")
    public void loadStoreHours(Callback<Map<String, Object>> cb) {
        db.collection("restaurants").document(uid()).get()
          .addOnSuccessListener(doc -> {
              if (doc.exists() && doc.contains("storeHours")) {
                  cb.onSuccess((Map<String, Object>) doc.get("storeHours"));
              } else {
                  cb.onSuccess(new HashMap<>());
              }
          })
          .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void isPhoneTaken(String phone, Callback<Boolean> cb) {
        db.collection("restaurants")
          .whereEqualTo("phone", phone)
          .get()
          .addOnSuccessListener(snaps -> {
              String currentUid = uid();
              boolean taken = false;
              for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snaps) {
                  if (!doc.getId().equals(currentUid)) { taken = true; break; }
              }
              cb.onSuccess(taken);
          })
          .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void isContactEmailTaken(String email, Callback<Boolean> cb) {
        db.collection("restaurants")
          .whereEqualTo("mail", email)
          .get()
          .addOnSuccessListener(snaps -> {
              String currentUid = uid();
              boolean taken = false;
              for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snaps) {
                  if (!doc.getId().equals(currentUid)) { taken = true; break; }
              }
              cb.onSuccess(taken);
          })
          .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void saveStoreHours(Map<String, Object> hours, Callback<Void> cb) {
        db.collection("restaurants").document(uid())
          .update("storeHours", hours)
          .addOnSuccessListener(v -> cb.onSuccess(null))
          .addOnFailureListener(e -> {
              Map<String, Object> wrapper = new HashMap<>();
              wrapper.put("storeHours", hours);
              db.collection("restaurants").document(uid())
                .set(wrapper, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(v2 -> cb.onSuccess(null))
                .addOnFailureListener(e2 -> cb.onFailure(e2.getMessage()));
          });
    }
}
