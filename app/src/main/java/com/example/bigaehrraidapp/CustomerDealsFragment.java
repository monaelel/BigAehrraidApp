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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CustomerDealsFragment extends Fragment {

    private LinearLayout addressBar;
    private TextView tvAddress;
    private LinearLayout rewardsBox;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST = 1002;
    private static final String DEFAULT_ADDRESS = "4890 Circle Rd, Montreal, QC H3W 1Z7";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_deals, container, false);

        addressBar = view.findViewById(R.id.addressBar);
        tvAddress = view.findViewById(R.id.tvAddress);
        rewardsBox = view.findViewById(R.id.rewardsBox);
        RecyclerView rvCategories = view.findViewById(R.id.rvCategories);

        List<Category> categories = Category.getFixedCategories();
        CategoryHomeAdapter adapter = new CategoryHomeAdapter(categories);
        rvCategories.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 3));
        rvCategories.setAdapter(adapter);
        adapter.setOnCategoryClickListener(cat -> {
            Intent intent = new Intent(requireActivity(), RestaurantListActivity.class);
            intent.putExtra(RestaurantListActivity.EXTRA_CATEGORY_TAG, cat.name);
            intent.putExtra(RestaurantListActivity.EXTRA_CATEGORY_NAME, cat.name);
            startActivity(intent);
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
        addressBar.setOnClickListener(v -> showAddressEditDialog());
    }

    private void showAddressEditDialog() {
        EditText input = new EditText(requireContext());
        input.setText(tvAddress.getText());
        input.setSelection(input.getText().length());
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setSingleLine(true);

        new AlertDialog.Builder(requireContext())
                .setTitle("Delivery Address")
                .setView(input)
                .setPositiveButton("Confirm", (d, w) -> {
                    String entered = input.getText().toString().trim();
                    if (!entered.isEmpty()) tvAddress.setText(entered);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchCurrentAddress() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvAddress.setText(DEFAULT_ADDRESS);
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                tvAddress.setText(DEFAULT_ADDRESS);
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
                    } else {
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            activity.runOnUiThread(() -> tvAddress.setText(DEFAULT_ADDRESS));
                        }
                    }
                } catch (IOException | IllegalStateException ignored) {
                    Activity activity = getActivity();
                    if (activity != null && isAdded()) {
                        activity.runOnUiThread(() -> tvAddress.setText(DEFAULT_ADDRESS));
                    }
                }
            }).start();
        });
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

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ActivityAuthLogin.class)));

        btnJoinUs.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ActivityAuthRegister.class)));
    }
}
