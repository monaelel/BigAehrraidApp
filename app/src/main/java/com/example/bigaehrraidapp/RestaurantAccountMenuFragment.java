package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RestaurantAccountMenuFragment extends Fragment {

    Button btnAccountInformation, btnManageStoreHours, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_account_menu, container, false);

        btnAccountInformation = view.findViewById(R.id.btnAccountInformation);
        btnManageStoreHours   = view.findViewById(R.id.btnManageStoreHours);
        btnLogout             = view.findViewById(R.id.btnLogout);

        btnAccountInformation.setOnClickListener(v ->
                ((RestaurantMainActivity) requireActivity())
                        .navigateTo(new RestaurantAccountFragment())
        );

        btnManageStoreHours.setOnClickListener(v ->
                ((RestaurantMainActivity) requireActivity())
                        .navigateTo(new RestaurantStoreHoursFragment())
        );

        btnLogout.setOnClickListener(v -> {
            AuthRepository.getInstance(requireContext()).logout();
            Intent intent = new Intent(requireContext(), CustomerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
