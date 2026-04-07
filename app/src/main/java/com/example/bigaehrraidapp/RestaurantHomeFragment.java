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

public class RestaurantHomeFragment extends Fragment {

    TextView tvTotalSales, tvOrderVolume, tvTicketSize;
    SalesBarChartView salesBarChart;
    Button btnManageOrder;

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

        // TODO: replace with real data from backend
        tvTotalSales.setText("$ 4,507.55");
        tvOrderVolume.setText("193");
        tvTicketSize.setText("$23.35");

        // Sample hourly sales data (24 values, hour 0 → 23)
        salesBarChart.setValues(new float[]{
                2,  1,  1,  0,  0,  1,
                4,  8, 12, 18, 22, 28,
               35, 40, 38, 30, 42, 55,
               48, 60, 58, 45, 30, 15
        });

        btnManageOrder.setOnClickListener(v ->
                startActivity(new Intent(getContext(), ManageOrderActivity.class))
        );

        return view;
    }
}
