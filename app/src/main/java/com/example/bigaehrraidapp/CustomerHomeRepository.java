package com.example.bigaehrraidapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
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

    public void loadAllRestaurantsForMap(Callback<List<Restaurant>> cb) {
        db.collection("restaurants").get()
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()))
            .addOnSuccessListener(snaps -> {
                List<Restaurant> result = new ArrayList<>();
                Log.d("HOME_DEBUG", "Restaurants for map fetched: " + snaps.size());
                for (QueryDocumentSnapshot doc : snaps) {
                    Restaurant r = new Restaurant();
                    r.id    = doc.getId();
                    r.name  = doc.getString("name");
                    r.email = doc.getString("email");
                    r.phone = doc.getString("phone");
                    Double lat = doc.getDouble("latitude");
                    Double lng = doc.getDouble("longitude");
                    if (lat == null || lng == null) {
                        lat = doc.getDouble("lat");
                        lng = doc.getDouble("lng");
                    }
                    if (lat != null && lng != null) {
                        r.latitude  = lat;
                        r.longitude = lng;
                        Log.d("HOME_DEBUG", "  restaurant marker: " + r.name + " (" + r.id + ") at " + lat + "," + lng);
                        result.add(r);
                    }
                }
                cb.onSuccess(result);
            });
    }

    public void loadRestaurantsByCategory(String categoryName, Callback<List<Restaurant>> cb) {
        db.collection("restaurants").get()
            .addOnFailureListener(e -> cb.onFailure(e.getMessage()))
            .addOnSuccessListener(restaurantSnaps -> {
                if (restaurantSnaps.isEmpty()) {
                    cb.onSuccess(new ArrayList<>());
                    return;
                }

                List<Restaurant> result = new ArrayList<>();
                AtomicInteger remaining = new AtomicInteger(restaurantSnaps.size());

                for (QueryDocumentSnapshot restDoc : restaurantSnaps) {
                    final String restaurantId = restDoc.getId();
                    final String restaurantName = restDoc.getString("name");
                    final String restaurantEmail = restDoc.getString("email");
                    final String restaurantPhone = restDoc.getString("phone");

                    db.collection("restaurants")
                        .document(restaurantId)
                        .collection("products")
                        .whereEqualTo("categoryName", categoryName)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(prodSnaps -> {
                            if (!prodSnaps.isEmpty()) {
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
