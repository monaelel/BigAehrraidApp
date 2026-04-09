package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class RestaurantHomeFragment extends Fragment {

    TextView          tvTotalSales, tvOrderVolume, tvTicketSize;
    SalesBarChartView salesBarChart;
    Button            btnManageOrder;

    AuthRepository  authRepo;
    OrderRepository orderRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_home, container, false);

        tvTotalSales   = view.findViewById(R.id.tvTotalSales);
        tvOrderVolume  = view.findViewById(R.id.tvOrderVolume);
        tvTicketSize   = view.findViewById(R.id.tvTicketSize);
        salesBarChart  = view.findViewById(R.id.salesBarChart);
        btnManageOrder = view.findViewById(R.id.btnManageOrder);

        authRepo  = AuthRepository.getInstance(requireContext());
        orderRepo = OrderRepository.getInstance(authRepo);

        loadTodayStats();

        btnManageOrder.setOnClickListener(v ->
                startActivity(new Intent(getContext(), ManageOrderActivity.class))
        );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodayStats(); // refresh when returning from ManageOrder
    }

    private void loadTodayStats() {
        String uid = authRepo.getCurrentUserId();
        if (uid == null) return;

        orderRepo.getTodayStats(uid, new OrderRepository.StatsCallback() {
            @Override
            public void onSuccess(double totalSales, int orderVolume,
                                  double ticketSize, float[] hourlyData) {
                tvTotalSales.setText(String.format(Locale.getDefault(), "$ %.2f", totalSales));
                tvOrderVolume.setText(String.valueOf(orderVolume));
                tvTicketSize.setText(String.format(Locale.getDefault(), "$ %.2f", ticketSize));
                salesBarChart.setValues(hourlyData);
            }

            @Override
            public void onFailure(String error) {
                tvTotalSales.setText("$ 0.00");
                tvOrderVolume.setText("0");
                tvTicketSize.setText("$ 0.00");
            }
        });
    }
}
