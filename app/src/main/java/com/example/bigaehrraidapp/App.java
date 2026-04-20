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
        com.stripe.android.PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51TOFvo8atuREOtKl9JXwXAwP3zTenyXKPdVQkncs2vWWvqbFEoviLjKbT5hgjJsmnV5aa9N5JAjYrWUAbAqvxa5H00PrfzsJ3g"
        );
        // Log all restaurants to Logcat for debugging
        RestaurantMigrationHelper.logAllRestaurants();
    }
}
