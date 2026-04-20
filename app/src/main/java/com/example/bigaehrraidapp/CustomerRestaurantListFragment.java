package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerRestaurantListFragment extends Fragment {

    private RecyclerView rvRestaurants;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<Restaurant> restaurants;
    private RestaurantListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_restaurant_list, container, false);

        rvRestaurants = view.findViewById(R.id.rvRestaurants);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        restaurants = new ArrayList<>();
        adapter = new RestaurantListAdapter(restaurants);
        adapter.setOnRestaurantClickListener(r -> {
            Intent intent = new Intent(requireActivity(), CustomerRestaurantMenuActivity.class);
            intent.putExtra(CustomerRestaurantMenuActivity.EXTRA_RESTAURANT_ID, r.id);
            intent.putExtra(CustomerRestaurantMenuActivity.EXTRA_RESTAURANT_NAME, r.name);
            startActivity(intent);
        });

        rvRestaurants.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRestaurants.setAdapter(adapter);

        loadRestaurants();

        return view;
    }

    private void loadRestaurants() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
            .collection("restaurants")
            .get()
            .addOnSuccessListener(snaps -> {
                restaurants.clear();
                for (QueryDocumentSnapshot doc : snaps) {
                    Restaurant r = new Restaurant();
                    r.id = doc.getId();
                    r.name = doc.getString("name");
                    r.email = doc.getString("email");
                    r.phone = doc.getString("phone");
                    r.mail = doc.getString("mail");

                    Object lat = doc.get("lat");
                    Object lng = doc.get("lng");
                    r.latitude = lat instanceof Number ? ((Number) lat).doubleValue() : 0.0;
                    r.longitude = lng instanceof Number ? ((Number) lng).doubleValue() : 0.0;

                    restaurants.add(r);
                }

                progressBar.setVisibility(View.GONE);
                if (restaurants.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setText("Failed to load restaurants");
                tvEmpty.setVisibility(View.VISIBLE);
            });
    }
}
