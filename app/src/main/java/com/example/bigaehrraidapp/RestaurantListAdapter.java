package com.example.bigaehrraidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.VH> {

    private final List<Restaurant> restaurants;

    public RestaurantListAdapter(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Restaurant r = restaurants.get(position);
        String name = r.name != null && !r.name.isEmpty() ? r.name : "Unnamed Restaurant";
        holder.tvName.setText(name);
        holder.tvInitial.setText(name.substring(0, 1).toUpperCase());
        holder.tvPhone.setText(r.phone != null && !r.phone.isEmpty() ? r.phone : r.email != null ? r.email : "");
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvInitial, tvName, tvPhone;

        VH(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvRestaurantInitial);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvPhone = itemView.findViewById(R.id.tvRestaurantPhone);
        }
    }
}
