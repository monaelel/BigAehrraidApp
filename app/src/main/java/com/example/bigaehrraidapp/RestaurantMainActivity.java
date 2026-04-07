package com.example.bigaehrraidapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RestaurantMainActivity extends AppCompatActivity {

    BottomNavigationView restaurantBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_main);

        restaurantBottomNav = findViewById(R.id.restaurantBottomNav);

        // Load default fragment
        loadFragment(new RestaurantHomeFragment());
        restaurantBottomNav.setSelectedItemId(R.id.nav_home);

        restaurantBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new RestaurantHomeFragment());
            } else if (id == R.id.nav_store) {
                loadFragment(new RestaurantStoreFragment());
            } else if (id == R.id.nav_account) {
                loadFragment(new RestaurantAccountFragment());
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.restaurantFragmentContainer, fragment)
                .commit();
    }
}
