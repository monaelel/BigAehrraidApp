package com.example.bigaehrraidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.VH> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private final List<Category> categories;
    private OnCategoryClickListener listener;

    private static final Map<String, String> EMOJI_MAP = new HashMap<>();

    static {
        EMOJI_MAP.put("burgers", "🍔");
        EMOJI_MAP.put("burger", "🍔");
        EMOJI_MAP.put("sides", "🍟");
        EMOJI_MAP.put("side", "🍟");
        EMOJI_MAP.put("drinks", "☕");
        EMOJI_MAP.put("drink", "☕");
        EMOJI_MAP.put("beverages", "🥤");
        EMOJI_MAP.put("beverage", "🥤");
        EMOJI_MAP.put("pizza", "🍕");
        EMOJI_MAP.put("chicken", "🍗");
        EMOJI_MAP.put("desserts", "🍰");
        EMOJI_MAP.put("dessert", "🍰");
        EMOJI_MAP.put("salads", "🥗");
        EMOJI_MAP.put("salad", "🥗");
        EMOJI_MAP.put("sandwiches", "🥪");
        EMOJI_MAP.put("sandwich", "🥪");
        EMOJI_MAP.put("tacos", "🌮");
        EMOJI_MAP.put("taco", "🌮");
        EMOJI_MAP.put("sushi", "🍣");
        EMOJI_MAP.put("pasta", "🍝");
        EMOJI_MAP.put("soup", "🍜");
        EMOJI_MAP.put("breakfast", "🍳");
        EMOJI_MAP.put("snacks", "🍿");
        EMOJI_MAP.put("snack", "🍿");
    }

    public CategoryHomeAdapter(List<Category> categories) {
        this.categories = categories;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_home, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Category cat = categories.get(position);
        String tag = cat.canonicalTag != null ? cat.canonicalTag.toLowerCase().trim() : "";
        String emoji = EMOJI_MAP.getOrDefault(tag, "🍽️");
        holder.tvEmoji.setText(emoji);
        holder.tvName.setText(cat.name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(cat);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvEmoji;
        final TextView tvName;

        VH(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvCategoryEmoji);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
