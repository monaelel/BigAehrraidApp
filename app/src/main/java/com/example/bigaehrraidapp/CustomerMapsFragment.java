package com.example.bigaehrraidapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerMapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private EditText etMapSearch;
    private LinearLayout restaurantCard;
    private TextView tvCardName, tvCardAddress, tvCardRating;

    private final Map<Marker, Restaurant> markerRestaurantMap = new HashMap<>();

    private static final int    LOCATION_PERMISSION_REQUEST = 2001;
    private static final double FALLBACK_LAT = 45.502564;
    private static final double FALLBACK_LNG = -73.534244;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_maps, container, false);

        etMapSearch    = view.findViewById(R.id.etMapSearch);
        restaurantCard = view.findViewById(R.id.restaurantCard);
        tvCardName     = view.findViewById(R.id.tvRestaurantCardName);
        tvCardAddress  = view.findViewById(R.id.tvRestaurantCardAddress);
        tvCardRating   = view.findViewById(R.id.tvRestaurantCardRating);

        etMapSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchRestaurantByName(etMapSearch.getText().toString().trim());
                InputMethodManager imm = (InputMethodManager)
                        requireActivity().getSystemService(android.app.Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etMapSearch.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapContainer);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        googleMap.setOnMarkerClickListener(marker -> {
            Restaurant r = markerRestaurantMap.get(marker);
            if (r != null) showRestaurantCard(r);
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> restaurantCard.setVisibility(View.GONE));

        LatLng defaultLocation = new LatLng(FALLBACK_LAT, FALLBACK_LNG);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        enableMyLocation();
        loadRestaurantMarkers();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void loadRestaurantMarkers() {
        CustomerHomeRepository.getInstance().loadAllRestaurantsForMap(
                new CustomerHomeRepository.Callback<List<Restaurant>>() {
                    @Override
                    public void onSuccess(List<Restaurant> data) {
                        if (googleMap == null || !isAdded()) return;
                        for (Restaurant r : data) {
                            if (r.latitude != 0.0 || r.longitude != 0.0) {
                                addMarker(r, r.latitude, r.longitude);
                            } else {
                                geocodeAndAddMarker(r);
                            }
                        }
                    }

                    @Override
                    public void onFailure(String error) {}
                });
    }

    private void geocodeAndAddMarker(Restaurant r) {
        String address = r.street + ", " + r.city + ", " + r.province + " " + r.postalCode + ", Canada";
        executor.execute(() -> {
            double lat = FALLBACK_LAT;
            double lng = FALLBACK_LNG;
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(address, 1);
                if (results != null && !results.isEmpty()) {
                    lat = results.get(0).getLatitude();
                    lng = results.get(0).getLongitude();
                }
            } catch (IOException | IllegalStateException ignored) {}

            final double finalLat = lat;
            final double finalLng = lng;
            CustomerHomeRepository.getInstance().updateCoordinates(r.id, finalLat, finalLng);
            mainHandler.post(() -> {
                if (isAdded() && googleMap != null) {
                    addMarker(r, finalLat, finalLng);
                }
            });
        });
    }

    private void addMarker(Restaurant r, double lat, double lng) {
        Marker marker = googleMap.addMarker(
                new MarkerOptions().position(new LatLng(lat, lng)).title(r.name));
        if (marker != null) markerRestaurantMap.put(marker, r);
    }

    private void searchRestaurantByName(String query) {
        if (query.isEmpty() || googleMap == null) return;
        String lower = query.toLowerCase();
        for (Map.Entry<Marker, Restaurant> entry : markerRestaurantMap.entrySet()) {
            Restaurant r = entry.getValue();
            if (r.name != null && r.name.toLowerCase().contains(lower)) {
                Marker marker = entry.getKey();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15f));
                showRestaurantCard(r);
                return;
            }
        }
    }

    private void showRestaurantCard(Restaurant r) {
        tvCardName.setText(r.name != null ? r.name : "Restaurant");
        tvCardRating.setText("★★★★☆ 4.5");
        tvCardAddress.setText(r.phone != null && !r.phone.isEmpty() ? r.phone : "");
        restaurantCard.setVisibility(View.VISIBLE);
        restaurantCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CustomerRestaurantMenuActivity.class);
            intent.putExtra(CustomerRestaurantMenuActivity.EXTRA_RESTAURANT_ID, r.id);
            intent.putExtra(CustomerRestaurantMenuActivity.EXTRA_RESTAURANT_NAME, r.name);
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
