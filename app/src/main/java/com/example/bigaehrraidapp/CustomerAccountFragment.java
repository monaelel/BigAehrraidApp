package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CustomerAccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_account, container, false);

        Button btnAccountInformation = view.findViewById(R.id.btnAccountInformation);
        Button btnOrderHistory      = view.findViewById(R.id.btnOrderHistory);
        Button btnLogout             = view.findViewById(R.id.btnLogout);
        Button btnLogin              = view.findViewById(R.id.btnLogin);

        AuthRepository authRepo = AuthRepository.getInstance(requireContext());
        boolean isLoggedIn = authRepo.isLoggedIn();

        if (isLoggedIn) {
            // Show account info + logout; hide login
            btnAccountInformation.setVisibility(View.VISIBLE);
            btnOrderHistory.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);

            btnAccountInformation.setOnClickListener(v ->
                Toast.makeText(getContext(), "Account Information — coming soon", Toast.LENGTH_SHORT).show()
            );

            btnOrderHistory.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), CustomerOrderHistoryActivity.class);
                startActivity(intent);
            });

            btnLogout.setOnClickListener(v -> {
                authRepo.logout();
                Intent intent = new Intent(requireContext(), CustomerMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        } else {
            // Guest mode — show login only
            btnAccountInformation.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);

            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ActivityAuthLogin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }

        return view;
    }
}
