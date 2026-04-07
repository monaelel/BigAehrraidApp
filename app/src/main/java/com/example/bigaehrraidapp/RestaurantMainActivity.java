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

        loadFragment(new RestaurantHomeFragment());
        restaurantBottomNav.setSelectedItemId(R.id.nav_home);

        restaurantBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new RestaurantHomeFragment());
            } else if (id == R.id.nav_store) {
                loadFragment(new RestaurantStoreFragment());
            } else if (id == R.id.nav_account) {
                loadFragment(new RestaurantAccountMenuFragment());
            }
            return true;
        });
    }

    // Root-level navigation (no back stack) — used by bottom nav tabs
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.restaurantFragmentContainer, fragment)
                .commit();
    }

    // Sub-navigation (with back stack) — used by in-tab sub-screens
    public void navigateTo(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.restaurantFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
