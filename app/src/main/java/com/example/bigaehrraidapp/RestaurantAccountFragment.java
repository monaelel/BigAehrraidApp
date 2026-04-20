package com.example.bigaehrraidapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public class RestaurantAccountFragment extends Fragment {

    TextView tvRestaurantName, tvRestaurantEmail, tvAddressName, tvAddressFull, tvPhone, tvMail;
    Button   btnEditInformation;

    RestaurantRepository restaurantRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_account, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        tvRestaurantName  = view.findViewById(R.id.tvRestaurantName);
        tvRestaurantEmail = view.findViewById(R.id.tvRestaurantEmail);
        tvAddressName     = view.findViewById(R.id.tvAddressName);
        tvAddressFull     = view.findViewById(R.id.tvAddressFull);
        tvPhone           = view.findViewById(R.id.tvPhone);
        tvMail            = view.findViewById(R.id.tvMail);
        btnEditInformation = view.findViewById(R.id.btnEditInformation);

        AuthRepository    authRepo   = AuthRepository.getInstance(requireContext());
        restaurantRepo = RestaurantRepository.getInstance(authRepo);

        loadProfile();

        btnEditInformation.setOnClickListener(v ->
                Toast.makeText(getContext(), "Please call admin to modify your restaurant!", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void loadProfile() {
        restaurantRepo.loadProfile(new RestaurantRepository.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                if (data == null || data.isEmpty()) return;

                String name  = getStr(data, "name");
                String email = getStr(data, "email");
                String phone = getStr(data, "phone");
                String mail  = getStr(data, "mail");

                String neighborhood, street, city, province, postalCode;

                // Check if address is nested (old seeded format) or flat (new format)
                Object addrObj = data.get("address");
                if (addrObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> addr = (Map<String, Object>) addrObj;
                    neighborhood = getStr(addr, "label");
                    street = getStr(addr, "street");
                    city = getStr(addr, "city");
                    province = getStr(addr, "province");
                    postalCode = getStr(addr, "postalCode");
                } else {
                    // Flat format
                    neighborhood = getStr(data, "neighborhood");
                    street = getStr(data, "street");
                    city = getStr(data, "city");
                    province = getStr(data, "province");
                    postalCode = getStr(data, "postalCode");
                }

                // If fields are empty, populate with defaults
                boolean needsUpdate = name.isEmpty() || phone.isEmpty() || mail.isEmpty() ||
                                      neighborhood.isEmpty() || street.isEmpty() || city.isEmpty() ||
                                      province.isEmpty() || postalCode.isEmpty();

                if (needsUpdate) {
                    Map<String, Object> updateData = new HashMap<>();
                    if (name.isEmpty()) updateData.put("name", "[ Restaurant Name ]");
                    if (phone.isEmpty()) updateData.put("phone", "[ Phone ]");
                    if (mail.isEmpty()) updateData.put("mail", email); // Use email as default
                    if (neighborhood.isEmpty()) updateData.put("neighborhood", "[ Neighbourhood ]");
                    if (street.isEmpty()) updateData.put("street", "[ Street ]");
                    if (city.isEmpty()) updateData.put("city", "[ City ]");
                    if (province.isEmpty()) updateData.put("province", "[ Province ]");
                    if (postalCode.isEmpty()) updateData.put("postalCode", "[ Postal Code ]");
                    updateData.put("lat", 0.0);
                    updateData.put("lng", 0.0);

                    restaurantRepo.saveProfile(updateData, new RestaurantRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            // Reload after update
                            loadProfile();
                        }
                        @Override
                        public void onFailure(String error) {
                            // Ignore or log
                        }
                    });
                    return; // Wait for reload
                }

                String label = neighborhood;
                String full = street + ", " + city + ", " + province + " " + postalCode + ", Canada";

                tvRestaurantName.setText(name.isEmpty()  ? "[ Restaurant Name ]" : name);
                tvRestaurantEmail.setText(email.isEmpty() ? "company@email.com"   : email);
                tvAddressName.setText(label.isEmpty()    ? "[ Neighbourhood ]"   : label);
                tvAddressFull.setText(full.isEmpty()     ? "[ Full address ]"    : full);
                tvPhone.setText(phone.isEmpty()          ? "[ Phone ]"           : phone);
                tvMail.setText(mail.isEmpty()            ? "[ Mail ]"            : mail);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Could not load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }
}
