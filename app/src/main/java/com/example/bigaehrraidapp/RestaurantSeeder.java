package com.example.bigaehrraidapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds pre-defined restaurant accounts into Firebase Auth + Firestore once on first launch.
 *
 * All accounts are created with password: 12345678
 *
 * Uses a secondary FirebaseApp so account creation does not sign out the current user.
 * Bump SEED_VERSION whenever you change the data — it will re-run on next launch.
 */
public class RestaurantSeeder {

    private static final String PREFS_NAME   = "seeder_prefs";
    private static final String KEY_VERSION  = "seed_version";
    private static final int    SEED_VERSION = 10;

    static final String DEFAULT_PASSWORD = "12345678";

    // ── Pre-defined restaurant data ───────────────────────────────────────────

    private static final List<Map<String, Object>> SEEDS = Arrays.asList(

        restaurant(
            "La Belle Poutine",
            "labelle@bigaehrraid.com",
            "+1 (514) 555-0101",
            "contact@labelle.ca",
            "Plateau",       "4321 Rue Saint-Denis", "Montréal", "QC", "H2J 2K9",
            45.5270, -73.5870
        ),
        restaurant(
            "Sakura Sushi",
            "sakura@bigaehrraid.com",
            "+1 (514) 555-0202",
            "hello@sakurasushi.ca",
            "Downtown",      "1010 Rue Peel",         "Montréal", "QC", "H3C 0G1",
            45.4980, -73.5714
        ),
        restaurant(
            "The Burger Spot",
            "burgerspot@bigaehrraid.com",
            "+1 (514) 555-0303",
            "info@burgerspot.ca",
            "Snowdon",       "5053 Ponsard Av",       "Montréal", "QC", "H3W 1A7",
            45.4902, -73.6201
        ),
        restaurant(
            "Nonna's Pizza",
            "nonna@bigaehrraid.com",
            "+1 (514) 555-0404",
            "order@nonnaspizza.ca",
            "Mile End",      "50 Avenue Bernard",     "Montréal", "QC", "H2V 1V9",
            45.5190, -73.6003
        ),
        restaurant(
            "Green Bowl",
            "greenbowl@bigaehrraid.com",
            "+1 (438) 555-0505",
            "support@greenbowl.ca",
            "Griffintown",   "750 Rue William",       "Montréal", "QC", "H3C 1N9",
            45.4903, -73.5623
        ),
        restaurant(
            "B12 Burgers",
            "b12@burgers.com",
            "+1 (514) 555-0101",
            "contact@burgers.com",
            "Sainte Catherine", "1826 Rue Sainte-Catherine O", "Montreal", "QC", "H3H 1M1",
            45.4930, -73.5800
        )
    );

    // ── Public API ────────────────────────────────────────────────────────────

    /** Call from Application.onCreate. Re-runs only when SEED_VERSION is bumped. */
    public static void seedIfNeeded(Context context) {
        android.util.Log.e("MANUEL_DEBUG", "seedIfNeeded called.");
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedVersion = prefs.getInt(KEY_VERSION, 0);
        android.util.Log.e("MANUEL_DEBUG", "Current saved version: " + savedVersion + ", Target version: " + SEED_VERSION);

        if (savedVersion >= SEED_VERSION) {
            android.util.Log.e("MANUEL_DEBUG", "Version check: OK. Checking if restaurants exist anyway.");
            ensureRestaurantsExist(context, prefs);
            return;
        }

        android.util.Log.e("MANUEL_DEBUG", "Version check: BUMP REQUIRED. Running seeder.");
        runSeeder(context, prefs);
    }

    private static void ensureRestaurantsExist(Context context, SharedPreferences prefs) {
        android.util.Log.e("MANUEL_DEBUG", "ensureRestaurantsExist: Fetching restaurants from Firestore...");
        FirebaseFirestore.getInstance()
            .collection("restaurants")
            .limit(1)
            .get()
            .addOnSuccessListener(snaps -> {
                if (snaps.isEmpty()) {
                    android.util.Log.e("MANUEL_DEBUG", "ensureRestaurantsExist: Collection is EMPTY. Running seeder.");
                    runSeeder(context, prefs);
                } else {
                    android.util.Log.e("MANUEL_DEBUG", "ensureRestaurantsExist: Collection is NOT empty. Running repair.");
                    repairRestaurantDocs();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("MANUEL_DEBUG", "ensureRestaurantsExist: FAILED to fetch collection: " + e.getMessage());
                // If the check fails, still try seeding.
                runSeeder(context, prefs);
            });
    }

    public static void runSeeder(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        runSeeder(context, prefs);
    }

    public static void runSeeder(Context context, SharedPreferences prefs) {
        android.util.Log.e("MANUEL_DEBUG", "runSeeder started.");
        // Use a secondary FirebaseApp so we don't disturb the current auth session
        FirebaseApp secondary;
        try {
            secondary = FirebaseApp.getInstance("seeder");
        } catch (IllegalStateException e) {
            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            secondary = FirebaseApp.initializeApp(context, options, "seeder");
        }

        FirebaseAuth      secondaryAuth = FirebaseAuth.getInstance(secondary);
        FirebaseFirestore db            = FirebaseFirestore.getInstance();

        createNext(secondaryAuth, db, 0, () ->
            prefs.edit().putInt(KEY_VERSION, SEED_VERSION).apply()
        );
    }

    /**
     * Repairs existing restaurant documents that contain no profile fields.
     * This is useful when the document exists only to hold subcollections.
     */
    public static void repairRestaurantDocs() {
        android.util.Log.e("MANUEL_DEBUG", "repairRestaurantDocs: Starting audit of all restaurant docs.");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants").get()
          .addOnSuccessListener(snaps -> {
              android.util.Log.e("MANUEL_DEBUG", "repairRestaurantDocs: Found " + snaps.size() + " documents to check.");
              for (QueryDocumentSnapshot doc : snaps) {
                  if (needsRepair(doc)) {
                      String uid = doc.getId();
                      String email = doc.getString("email");
                      android.util.Log.e("MANUEL_DEBUG", "repairRestaurantDocs: Doc " + uid + " needs repair. Email: " + email);
                      if (email != null && !email.isEmpty()) {
                          restoreFromSeed(uid, email);
                      } else {
                          db.collection("users").document(uid).get()
                            .addOnSuccessListener(userDoc -> {
                                String userEmail = userDoc.getString("email");
                                if (userEmail != null && !userEmail.isEmpty()) {
                                    android.util.Log.e("MANUEL_DEBUG", "repairRestaurantDocs: Found email in users collection for " + uid + ": " + userEmail);
                                    restoreFromSeed(uid, userEmail);
                                }
                            });
                      }
                  }
              }
          })
          .addOnFailureListener(e -> {
              android.util.Log.e("MANUEL_DEBUG", "repairRestaurantDocs: FAILED to list restaurants: " + e.getMessage());
          });
    }

    private static boolean needsRepair(QueryDocumentSnapshot doc) {
        return doc.getData().isEmpty() || doc.getString("name") == null || doc.getString("email") == null;
    }

    private static void restoreFromSeed(String uid, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        loadSeedForEmail(email, new SeedCallback() {
            @Override
            public void onFound(Map<String, Object> data) {
                android.util.Log.e("MANUEL_DEBUG", "restoreFromSeed: Found seed data for " + email + ". Restoring doc " + uid);
                Map<String, Object> restaurantDoc = new HashMap<>(data);
                restaurantDoc.put("email", email);
                db.collection("restaurants").document(uid)
                  .set(restaurantDoc, SetOptions.merge());
                  
                Map<String, Object> userDoc = new HashMap<>();
                userDoc.put("email", email);
                userDoc.put("role", "restaurant");
                db.collection("users").document(uid).set(userDoc, SetOptions.merge());
            }

            @Override
            public void onNotFound() {
                Map<String, Object> restaurantDoc = new HashMap<>();
                restaurantDoc.put("email", email);
                restaurantDoc.put("name", "");
                restaurantDoc.put("phone", "");
                restaurantDoc.put("mail", email);
                restaurantDoc.put("neighborhood", "");
                restaurantDoc.put("street", "");
                restaurantDoc.put("city", "");
                restaurantDoc.put("province", "");
                restaurantDoc.put("postalCode", "");
                restaurantDoc.put("latitude", 0.0);
                restaurantDoc.put("longitude", 0.0);
                db.collection("restaurants").document(uid)
                  .set(restaurantDoc, SetOptions.merge());
            }
        });
    }

    /** Called by AuthRepository when a restaurant owner registers. */
    public static void loadSeedForEmail(String email, SeedCallback cb) {
        FirebaseFirestore.getInstance()
            .collection("restaurantSeeds")
            .document(emailToDocId(email))
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) cb.onFound(doc.getData());
                else              cb.onNotFound();
            })
            .addOnFailureListener(e -> cb.onNotFound());
    }

    public interface SeedCallback {
        void onFound(Map<String, Object> data);
        void onNotFound();
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private static void createNext(FirebaseAuth auth, FirebaseFirestore db,
                                   int index, Runnable onAllDone) {
        if (index >= SEEDS.size()) {
            android.util.Log.e("MANUEL_DEBUG", "All seeds processed. Done.");
            onAllDone.run();
            return;
        }

        Map<String, Object> seed  = SEEDS.get(index);
        String              email = (String) seed.get("email");

        android.util.Log.e("MANUEL_DEBUG", "Attempting to create user for email: " + email);
        auth.createUserWithEmailAndPassword(email, DEFAULT_PASSWORD)
            .addOnSuccessListener(result -> {
                android.util.Log.e("MANUEL_DEBUG", "SUCCESS! Created Auth user for: " + email);
                // New account — write all documents
                writeRestaurantDocs(auth, db, result.getUser().getUid(), email, seed);
                createNext(auth, db, index + 1, onAllDone);
            })
            .addOnFailureListener(createErr -> {
                android.util.Log.e("MANUEL_DEBUG", "ERROR! Auth creation failed for " + email + ": " + createErr.getMessage() + ". Attempting sign-in...");
                // Account already exists — sign in to get the UID, then update the docs
                auth.signInWithEmailAndPassword(email, DEFAULT_PASSWORD)
                    .addOnSuccessListener(signInResult -> {
                        android.util.Log.e("MANUEL_DEBUG", "SUCCESS! Sign-in successful for: " + email);
                        writeRestaurantDocs(auth, db, signInResult.getUser().getUid(), email, seed);
                        createNext(auth, db, index + 1, onAllDone);
                    })
                    .addOnFailureListener(signInErr -> {
                        android.util.Log.e("MANUEL_DEBUG", "ERROR! Sign-in also failed for " + email + ": " + signInErr.getMessage());
                        // Can't sign in either (wrong password or no network) — skip
                        auth.signOut();
                        createNext(auth, db, index + 1, onAllDone);
                    });
            });
    }

    private static void writeRestaurantDocs(FirebaseAuth auth, FirebaseFirestore db,
                                            String uid, String email,
                                            Map<String, Object> seed) {
        auth.signOut();

        // users/{uid}
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("email", email);
        userDoc.put("role",  "restaurant");
        
        android.util.Log.e("MANUEL_DEBUG", "Role assigned to " + email + ": restaurant");
        
        db.collection("users").document(uid).set(userDoc, SetOptions.merge())
          .addOnSuccessListener(aVoid -> android.util.Log.e("MANUEL_DEBUG", "writeRestaurantDocs: Wrote users/" + uid + " with role: restaurant"))
          .addOnFailureListener(e -> android.util.Log.e("MANUEL_DEBUG", "writeRestaurantDocs: FAILED users/" + uid + ": " + e.getMessage()));

        // restaurants/{uid}  — full profile data
        Map<String, Object> restaurantDoc = new HashMap<>(seed);
        restaurantDoc.put("email", email);
        db.collection("restaurants").document(uid).set(restaurantDoc)
          .addOnSuccessListener(aVoid -> android.util.Log.e("MANUEL_DEBUG", "writeRestaurantDocs: Wrote restaurants/" + uid))
          .addOnFailureListener(e -> android.util.Log.e("MANUEL_DEBUG", "writeRestaurantDocs: FAILED restaurants/" + uid + ": " + e.getMessage()));

        // restaurantSeeds/{emailId}  — used when owner re-registers
        db.collection("restaurantSeeds").document(emailToDocId(email)).set(seed)
          .addOnSuccessListener(aVoid -> android.util.Log.e("MANUEL_DEBUG", "writeRestaurantDocs: Wrote restaurantSeeds/" + emailToDocId(email)))
          .addOnFailureListener(e -> android.util.Log.e("MANUEL_DEBUG", "writeRestaurantDocs: FAILED restaurantSeeds/" + emailToDocId(email) + ": " + e.getMessage()));
    }

    private static Map<String, Object> restaurant(String name, String email,
                                                   String phone, String mail,
                                                   String neighbourhood,
                                                   String street, String city,
                                                   String province, String postalCode,
                                                   double latitude, double longitude) {
        Map<String, Object> data = new HashMap<>();
        data.put("name",        name);
        data.put("email",       email);
        data.put("phone",       phone);
        data.put("mail",        mail);
        data.put("neighborhood", neighbourhood);
        data.put("street",      street);
        data.put("city",        city);
        data.put("province",    province);
        data.put("postalCode",  postalCode);
        data.put("latitude",         latitude);
        data.put("longitude",         longitude);
        return data;
    }

    private static String emailToDocId(String email) {
        return email.replace(".", "_").replace("@", "__");
    }
}
