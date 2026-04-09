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
                Toast.makeText(getContext(), "Edit Information — coming soon", Toast.LENGTH_SHORT).show()
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

                // Address fields
                Object addrObj = data.get("address");
                String label = "", full = "";
                if (addrObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> addr = (Map<String, Object>) addrObj;
                    label = getStr(addr, "label");
                    full  = getStr(addr, "street") + ", " + getStr(addr, "city")
                          + ",\n" + getStr(addr, "province") + ", Canada, " + getStr(addr, "postalCode");
                }

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
