package com.example.bigaehrraidapp;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Helper class to identify restaurants in Firebase by their UIDs and names.
 * Use this to map Firestore document IDs to restaurant names for manual updates.
 */
public class RestaurantMigrationHelper {

    private static final String TAG = "RestaurantMigration";

    /**
     * Logs all restaurants with their UIDs and names for easy identification.
     * Call this from your Application.onCreate() or a debug activity.
     */
    public static void logAllRestaurants() {
        FirebaseFirestore.getInstance()
            .collection("restaurants")
            .get()
            .addOnSuccessListener(snaps -> {
                Log.d(TAG, "Fetched restaurant collection count: " + snaps.size());
                Log.d(TAG, "=== RESTAURANT MAPPING ===");
                if (snaps.isEmpty()) {
                    Log.d(TAG, "No documents found in 'restaurants' collection.");
                    Log.d(TAG, "If you only see documents in 'restaurantSeeds', those are NOT the live restaurant docs.");
                }
                for (QueryDocumentSnapshot doc : snaps) {
                    String uid = doc.getId();
                    String name = doc.getString("name");
                    String email = doc.getString("email");
                    Log.d(TAG, "UID: " + uid + " | NAME: " + name + " | EMAIL: " + email);
                }
                Log.d(TAG, "=== END MAPPING ===");
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch restaurants", e));
    }

    /**
     * Alternative: Print restaurant mapping as a formatted string.
     * Useful for debugging and manual Firestore updates.
     */
    public static void logRestaurantMapping() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    FIRESTORE RESTAURANT MAP                    ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");

        FirebaseFirestore.getInstance()
            .collection("restaurants")
            .get()
            .addOnSuccessListener(snaps -> {
                for (QueryDocumentSnapshot doc : snaps) {
                    String uid = doc.getId();
                    String name = doc.getString("name");
                    String email = doc.getString("email");
                    sb.append("║ UID: ").append(uid.substring(0, Math.min(8, uid.length())))
                        .append("... | NAME: ").append(padRight(name, 20))
                        .append(" ║\n");
                }
                sb.append("╚════════════════════════════════════════════════════════════════╝\n");
                Log.d(TAG, sb.toString());
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch restaurants", e));
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
