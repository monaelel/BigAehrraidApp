package com.example.bigaehrraidapp;

import android.app.Application;

import com.google.android.libraries.places.api.Places;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Ensure Firebase is initialized before seeder
        if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(this);
        }
        
        // Uncomment below to clear the seeder preferences programmatically
        // getSharedPreferences("seeder_prefs", MODE_PRIVATE).edit().clear().apply();

        // Disable App Check for debug builds on physical hardware
        com.google.firebase.appcheck.FirebaseAppCheck firebaseAppCheck = com.google.firebase.appcheck.FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
        );

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }
        com.stripe.android.PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51TOFvo8atuREOtKl9JXwXAwP3zTenyXKPdVQkncs2vWWvqbFEoviLjKbT5hgjJsmnV5aa9N5JAjYrWUAbAqvxa5H00PrfzsJ3g"
        );
        // Log all restaurants to Logcat for debugging
        RestaurantMigrationHelper.logAllRestaurants();
    }
}
