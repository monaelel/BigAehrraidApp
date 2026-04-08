package com.example.bigaehrraidapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RestaurantAccountFragment extends Fragment {

    TextView tvRestaurantName, tvRestaurantEmail, tvAddressName, tvAddressFull, tvPhone, tvMail;
    Button btnEditInformation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_account, container, false);

        tvRestaurantName = view.findViewById(R.id.tvRestaurantName);
        tvRestaurantEmail = view.findViewById(R.id.tvRestaurantEmail);
        tvAddressName = view.findViewById(R.id.tvAddressName);
        tvAddressFull = view.findViewById(R.id.tvAddressFull);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvMail = view.findViewById(R.id.tvMail);
        btnEditInformation = view.findViewById(R.id.btnEditInformation);

        btnEditInformation.setOnClickListener(v -> {
            // TODO: open edit information screen
        });

        return view;
    }
}
