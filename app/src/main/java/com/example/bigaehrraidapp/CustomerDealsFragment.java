package com.example.bigaehrraidapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CustomerDealsFragment extends Fragment {

    private LinearLayout addressBar;
    private TextView tvAddress;
    private LinearLayout rewardsBox;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;
    private static final int LOCATION_PERMISSION_REQUEST = 1002;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_deals, container, false);

        addressBar = view.findViewById(R.id.addressBar);
        tvAddress = view.findViewById(R.id.tvAddress);
        rewardsBox = view.findViewById(R.id.rewardsBox);
        RecyclerView rvCategories = view.findViewById(R.id.rvCategories);

        List<Category> categories = new java.util.ArrayList<>();
        CategoryHomeAdapter adapter = new CategoryHomeAdapter(categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(adapter);
        adapter.setOnCategoryClickListener(cat -> {
            Intent intent = new Intent(requireActivity(), RestaurantListActivity.class);
            intent.putExtra(RestaurantListActivity.EXTRA_CATEGORY_TAG, cat.name);
            intent.putExtra(RestaurantListActivity.EXTRA_CATEGORY_NAME, cat.name);
            startActivity(intent);
        });

        Log.d("HOME_DEBUG", "Starting loadAllCategories...");
        CustomerHomeRepository.getInstance().loadAllCategories(new CustomerHomeRepository.Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                Log.d("HOME_DEBUG", "onSuccess — categories received: " + data.size());
                for (Category c : data) Log.d("HOME_DEBUG", "  category: " + c.name);
                categories.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Log.e("HOME_DEBUG", "onFailure: " + error);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load categories: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });

        AuthRepository auth = AuthRepository.getInstance(requireContext());

        if (auth.isLoggedIn()) {
            addressBar.setVisibility(View.VISIBLE);
            rewardsBox.setVisibility(View.GONE);
            setupAddressBar();
        } else {
            addressBar.setVisibility(View.GONE);
            rewardsBox.setVisibility(View.VISIBLE);
            setupRewardsBox(view);
        }

        return view;
    }

    private void setupAddressBar() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        fetchCurrentAddress();
        addressBar.setOnClickListener(v -> launchAddressSearch());
    }

    private void fetchCurrentAddress() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvAddress.setText("Tap to set your address");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                tvAddress.setText("Tap to set your address");
                return;
            }
            new Thread(() -> {
                try {
                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String addressLine = addresses.get(0).getAddressLine(0);
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            activity.runOnUiThread(() -> tvAddress.setText(addressLine));
                        }
                    }
                } catch (IOException | IllegalStateException ignored) {
                    Activity activity = getActivity();
                    if (activity != null && isAdded()) {
                        activity.runOnUiThread(() -> tvAddress.setText("Tap to set your address"));
                    }
                }
            }).start();
        });
    }

    private void launchAddressSearch() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireActivity());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                String address = place.getAddress();
                if (address != null) {
                    tvAddress.setText(address);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentAddress();
        }
    }

    private void setupRewardsBox(View view) {
        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnJoinUs = view.findViewById(R.id.btnJoinUs);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RoleSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnJoinUs.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ActivityAuthRegister.class);
            intent.putExtra("role", "customer");
            startActivity(intent);
        });
    }
}
