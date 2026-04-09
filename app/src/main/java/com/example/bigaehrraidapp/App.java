package com.example.bigaehrraidapp;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RestaurantSeeder.seedIfNeeded(this);
    }
}
