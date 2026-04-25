package com.example.bigaehrraidapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductRepository {

    private static ProductRepository instance;
    private final FirebaseFirestore db;
    private final AuthRepository    authRepo;

    public interface Callback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    private ProductRepository(AuthRepository authRepo) {
        this.db       = FirebaseFirestore.getInstance();
        this.authRepo = authRepo;
    }

    public static synchronized ProductRepository getInstance(AuthRepository authRepo) {
        if (instance == null) instance = new ProductRepository(authRepo);
        return instance;
    }

    private com.google.firebase.firestore.CollectionReference productsRef() {
        return db.collection("restaurants")
                 .document(authRepo.getCurrentUserId())
                 .collection("products");
    }

    public void loadProducts(Callback<List<Map<String, Object>>> cb) {
        productsRef().get()
          .addOnSuccessListener(snapshots -> {
              List<Map<String, Object>> list = new ArrayList<>();
              for (QueryDocumentSnapshot doc : snapshots) {
                  Map<String, Object> data = doc.getData();
                  data.put("productId", doc.getId());
                  list.add(data);
              }
              cb.onSuccess(list);
          })
          .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void addProduct(Map<String, Object> product, Callback<String> cb) {
        String uid = authRepo.getCurrentUserId();
        if (uid == null) {
            cb.onFailure("Not logged in");
            return;
        }
        db.collection("restaurants").document(uid).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Map<String, Object> basicInfo = new HashMap<>();
                    basicInfo.put("name", "Unknown Restaurant");
                    basicInfo.put("lat", 0.0);
                    basicInfo.put("lng", 0.0);
                    db.collection("restaurants").document(uid).set(basicInfo);
                }
                productsRef().add(product)
                  .addOnSuccessListener(ref -> cb.onSuccess(ref.getId()))
                  .addOnFailureListener(e   -> cb.onFailure(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void updateProduct(String productId, Map<String, Object> data, Callback<Void> cb) {
        productsRef().document(productId)
          .update(data)
          .addOnSuccessListener(v -> cb.onSuccess(null))
          .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void deleteProduct(String productId, Callback<Void> cb) {
        productsRef().document(productId)
          .delete()
          .addOnSuccessListener(v -> cb.onSuccess(null))
          .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }
}
