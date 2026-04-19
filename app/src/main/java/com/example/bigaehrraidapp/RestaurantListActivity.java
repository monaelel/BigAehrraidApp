package com.example.bigaehrraidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RestaurantListActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_TAG = "category_tag";
    public static final String EXTRA_CATEGORY_NAME = "category_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        String categoryTag = getIntent().getStringExtra(EXTRA_CATEGORY_TAG);
        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);

        TextView tvTitle = findViewById(R.id.tvCategoryTitle);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rvRestaurants = findViewById(R.id.rvRestaurants);

        tvTitle.setText(categoryName != null ? categoryName : "Restaurants");
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        List<Restaurant> restaurants = new ArrayList<>();
        RestaurantListAdapter adapter = new RestaurantListAdapter(restaurants);
        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        rvRestaurants.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvRestaurants.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);

        CustomerHomeRepository.getInstance().loadRestaurantsByCategory(
                categoryTag != null ? categoryTag : "",
                new CustomerHomeRepository.Callback<List<Restaurant>>() {
                    @Override
                    public void onSuccess(List<Restaurant> data) {
                        progressBar.setVisibility(View.GONE);
                        if (data.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            restaurants.addAll(data);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }
}
