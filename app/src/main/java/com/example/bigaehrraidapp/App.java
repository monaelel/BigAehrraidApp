package com.example.bigaehrraidapp;

import android.app.Application;

import com.google.android.libraries.places.api.Places;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RestaurantSeeder.seedIfNeeded(this);
        RestaurantSeeder.repairRestaurantDocs();
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }
        // Log all restaurants to Logcat for debugging
        RestaurantMigrationHelper.logAllRestaurants();
    }
}
