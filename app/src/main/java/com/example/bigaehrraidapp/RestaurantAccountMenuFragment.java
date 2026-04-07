package com.example.bigaehrraidapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RestaurantAccountMenuFragment extends Fragment {

    Button btnAccountInformation, btnManageStoreHours;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_account_menu, container, false);

        btnAccountInformation = view.findViewById(R.id.btnAccountInformation);
        btnManageStoreHours = view.findViewById(R.id.btnManageStoreHours);

        btnAccountInformation.setOnClickListener(v ->
                ((RestaurantMainActivity) requireActivity())
                        .navigateTo(new RestaurantAccountFragment())
        );

        btnManageStoreHours.setOnClickListener(v ->
                ((RestaurantMainActivity) requireActivity())
                        .navigateTo(new RestaurantStoreHoursFragment())
        );

        return view;
    }
}
