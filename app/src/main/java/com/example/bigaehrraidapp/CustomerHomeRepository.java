package com.example.bigaehrraidapp;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomerHomeRepository {

    private static CustomerHomeRepository instance;
    private final FirebaseFirestore db;

    public interface Callback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    private CustomerHomeRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized CustomerHomeRepository getInstance() {
        if (instance == null) instance = new CustomerHomeRepository();
        return instance;
    }

    public void loadAllCategories(Callback<List<Category>> cb) {
        db.collection("restaurants").get()
            .addOnFailureListener(e -> {
                Log.e("HOME_DEBUG", "Failed to fetch restaurants: " + e.getMessage());
                cb.onFailure(e.getMessage());
            })
            .addOnSuccessListener(restaurantSnaps -> {
                Log.d("HOME_DEBUG", "Restaurants fetched: " + restaurantSnaps.size());
                if (restaurantSnaps.isEmpty()) {
                    cb.onSuccess(new ArrayList<>());
                    return;
                }

                Map<String, Category> deduped = new LinkedHashMap<>();
                AtomicInteger remaining = new AtomicInteger(restaurantSnaps.size());

                for (QueryDocumentSnapshot restDoc : restaurantSnaps) {
                    Log.d("HOME_DEBUG", "Fetching categories for restaurant: " + restDoc.getId());
                    db.collection("restaurants")
                        .document(restDoc.getId())
                        .collection("categories")
                        .get()
                        .addOnSuccessListener(catSnaps -> {
                            Log.d("HOME_DEBUG", "  -> categories found: " + catSnaps.size());
                            synchronized (deduped) {
                                for (QueryDocumentSnapshot catDoc : catSnaps) {
                                    String tag = catDoc.getString("canonicalTag");
                                    if (tag == null) tag = catDoc.getString("name");
                                    if (tag == null) continue;
                                    String key = tag.toLowerCase().trim();
                                    if (!deduped.containsKey(key)) {
                                        Category cat = new Category();
                                        cat.id = catDoc.getId();
                                        cat.name = catDoc.getString("name");
                                        cat.canonicalTag = tag;
                                        Long so = catDoc.getLong("sortOrder");
                                        cat.sortOrder = so != null ? so.intValue() : 0;
                                        deduped.put(key, cat);
                                    }
                                }
                            }
                            if (remaining.decrementAndGet() == 0) {
                                Log.d("HOME_DEBUG", "All done. Total unique categories: " + deduped.size());
                                cb.onSuccess(new ArrayList<>(deduped.values()));
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("HOME_DEBUG", "  -> failed to fetch categories: " + e.getMessage());
                            if (remaining.decrementAndGet() == 0) {
                                cb.onSuccess(new ArrayList<>(deduped.values()));
                            }
                        });
                }
            });
    }

    public void loadRestaurantsByCategory(String canonicalTag, Callback<List<Restaurant>> cb) {
        db.collection("restaurants").get()
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()))
            .addOnSuccessListener(restaurantSnaps -> {
                if (restaurantSnaps.isEmpty()) {
                    cb.onSuccess(new ArrayList<>());
                    return;
                }

                List<Restaurant> result = new ArrayList<>();
                AtomicInteger remaining = new AtomicInteger(restaurantSnaps.size());
                String targetKey = canonicalTag.toLowerCase().trim();

                for (QueryDocumentSnapshot restDoc : restaurantSnaps) {
                    final String restaurantId = restDoc.getId();
                    final String restaurantName = restDoc.getString("name");
                    final String restaurantEmail = restDoc.getString("email");
                    final String restaurantPhone = restDoc.getString("phone");

                    db.collection("restaurants")
                        .document(restaurantId)
                        .collection("categories")
                        .get()
                        .addOnSuccessListener(catSnaps -> {
                            boolean hasCategory = false;
                            for (QueryDocumentSnapshot catDoc : catSnaps) {
                                String tag = catDoc.getString("canonicalTag");
                                if (tag == null) tag = catDoc.getString("name");
                                if (tag == null) continue;
                                if (tag.toLowerCase().trim().equals(targetKey)) {
                                    hasCategory = true;
                                    break;
                                }
                            }
                            if (hasCategory) {
                                Restaurant r = new Restaurant();
                                r.id = restaurantId;
                                r.name = restaurantName;
                                r.email = restaurantEmail;
                                r.phone = restaurantPhone;
                                synchronized (result) {
                                    result.add(r);
                                }
                            }
                            if (remaining.decrementAndGet() == 0) {
                                cb.onSuccess(result);
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (remaining.decrementAndGet() == 0) {
                                cb.onSuccess(result);
                            }
                        });
                }
            });
    }
}
