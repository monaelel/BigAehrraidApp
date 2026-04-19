package com.example.bigaehrraidapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText etMapSearch;
    private LinearLayout restaurantCard;
    private TextView tvCardName, tvCardAddress;

    private final Map<String, Restaurant> markerRestaurantMap = new HashMap<>();

    private static final int LOCATION_PERMISSION_REQUEST = 2001;
    private static final int MAP_AUTOCOMPLETE_REQUEST    = 2002;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_maps, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        etMapSearch    = view.findViewById(R.id.etMapSearch);
        restaurantCard = view.findViewById(R.id.restaurantCard);
        tvCardName     = view.findViewById(R.id.tvRestaurantCardName);
        tvCardAddress  = view.findViewById(R.id.tvRestaurantCardAddress);

        etMapSearch.setOnClickListener(v -> launchPlaceSearch());
        etMapSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                launchPlaceSearch();
                return true;
            }
            return false;
        });
        etMapSearch.setFocusable(false);

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
            Restaurant r = markerRestaurantMap.get(marker.getId());
            if (r != null) showRestaurantCard(r);
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> restaurantCard.setVisibility(View.GONE));

        LatLng defaultLocation = new LatLng(45.5017, -73.5673);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        enableMyLocation();
        loadRestaurantMarkers();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            moveCameraToCurrentLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void moveCameraToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && googleMap != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f));
            }
        });
    }

    private void loadRestaurantMarkers() {
        CustomerHomeRepository.getInstance().loadAllRestaurantsForMap(
                new CustomerHomeRepository.Callback<List<Restaurant>>() {
                    @Override
                    public void onSuccess(List<Restaurant> data) {
                        if (googleMap == null || !isAdded()) return;
                        for (Restaurant r : data) {
                            LatLng pos = new LatLng(r.latitude, r.longitude);
                            Marker marker = googleMap.addMarker(
                                    new MarkerOptions().position(pos).title(r.name));
                            if (marker != null) {
                                markerRestaurantMap.put(marker.getId(), r);
                            }
                        }
                    }

                    @Override
                    public void onFailure(String error) {}
                });
    }

    private void showRestaurantCard(Restaurant r) {
        tvCardName.setText(r.name != null ? r.name : "Restaurant");
        tvCardAddress.setText(r.phone != null && !r.phone.isEmpty() ? r.phone : "");
        restaurantCard.setVisibility(View.VISIBLE);
        restaurantCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CustomerRestaurantMenuActivity.class);
            intent.putExtra(CustomerRestaurantMenuActivity.EXTRA_RESTAURANT_ID, r.id);
            intent.putExtra(CustomerRestaurantMenuActivity.EXTRA_RESTAURANT_NAME, r.name);
            startActivity(intent);
        });
    }

    private void launchPlaceSearch() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireActivity());
        startActivityForResult(intent, MAP_AUTOCOMPLETE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MAP_AUTOCOMPLETE_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                etMapSearch.setText(place.getName());

                LatLng latLng = place.getLatLng();
                if (latLng != null && googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
                }

                InputMethodManager imm = (InputMethodManager)
                        requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etMapSearch.getWindowToken(), 0);
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
            enableMyLocation();
        }
    }
}
