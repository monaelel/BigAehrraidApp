package com.example.bigaehrraidapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerMainActivity extends AppCompatActivity {

    BottomNavigationView customerBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_main);

        customerBottomNav = findViewById(R.id.customerBottomNav);

        // Load default fragment
        loadFragment(new CustomerDealsFragment());
        customerBottomNav.setSelectedItemId(R.id.nav_deals);

        customerBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_deals) {
                loadFragment(new CustomerDealsFragment());
            } else if (id == R.id.nav_maps) {
                loadFragment(new CustomerMapsFragment());
            } else if (id == R.id.nav_account) {
                loadFragment(new CustomerAccountFragment());
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.customerFragmentContainer, fragment)
                .commit();
    }
}
