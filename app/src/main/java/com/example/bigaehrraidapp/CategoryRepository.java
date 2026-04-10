package com.example.bigaehrraidapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryRepository {

    private static CategoryRepository instance;
    private final FirebaseFirestore db;
    private final AuthRepository authRepo;

    public interface Callback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    private CategoryRepository(AuthRepository authRepo) {
        this.db = FirebaseFirestore.getInstance();
        this.authRepo = authRepo;
    }

    public static synchronized CategoryRepository getInstance(AuthRepository authRepo) {
        if (instance == null) instance = new CategoryRepository(authRepo);
        return instance;
    }

    private com.google.firebase.firestore.CollectionReference categoriesRef() {
        return db.collection("restaurants")
                 .document(authRepo.getCurrentUserId())
                 .collection("categories");
    }

    public void loadCategories(Callback<List<Category>> cb) {
        categoriesRef().orderBy("sortOrder").get()
            .addOnSuccessListener(snapshots -> {
                List<Category> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Category cat = new Category();
                    cat.id           = doc.getId();
                    cat.name         = doc.getString("name");
                    cat.canonicalTag = doc.getString("canonicalTag");
                    Long so          = doc.getLong("sortOrder");
                    cat.sortOrder    = so != null ? so.intValue() : 0;
                    list.add(cat);
                }
                cb.onSuccess(list);
            })
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void addCategory(Map<String, Object> data, Callback<String> cb) {
        categoriesRef().add(data)
            .addOnSuccessListener(ref -> cb.onSuccess(ref.getId()))
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void updateCategory(String categoryId, Map<String, Object> data, Callback<Void> cb) {
        categoriesRef().document(categoryId)
            .update(data)
            .addOnSuccessListener(v -> cb.onSuccess(null))
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    public void deleteCategory(String categoryId, Callback<Void> cb) {
        categoriesRef().document(categoryId)
            .delete()
            .addOnSuccessListener(v -> cb.onSuccess(null))
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }
}
