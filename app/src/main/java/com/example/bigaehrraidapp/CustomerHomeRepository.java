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
                for (QueryDocumentSnapshot doc : snaps) {
                    Restaurant r = new Restaurant();
                    r.id         = doc.getId();
                    r.name       = doc.getString("name");
                    r.email      = doc.getString("email");
                    r.phone      = doc.getString("phone");
                    r.street     = doc.getString("street");
                    r.city       = doc.getString("city");
                    r.province   = doc.getString("province");
                    r.postalCode = doc.getString("postalCode");
                    Double lat = doc.getDouble("lat");
                    Double lng = doc.getDouble("lng");
                    r.latitude  = lat != null ? lat : 0.0;
                    r.longitude = lng != null ? lng : 0.0;
                    result.add(r);
                }
                cb.onSuccess(result);
            });
    }

    public void updateCoordinates(String restaurantId, double lat, double lng) {
        Map<String, Object> update = new HashMap<>();
        update.put("lat", lat);
        update.put("lng", lng);
        db.collection("restaurants").document(restaurantId).update(update);
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
