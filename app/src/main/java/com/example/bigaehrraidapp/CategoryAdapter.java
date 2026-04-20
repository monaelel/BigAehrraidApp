package com.example.bigaehrraidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnEditListener  { void onEdit(Category category); }
    public interface OnDeleteListener { void onDelete(String categoryId, int position); }

    private final List<Category> categories;
    private final OnEditListener editListener;
    private final OnDeleteListener deleteListener;

    public CategoryAdapter(List<Category> categories,
                           OnEditListener editListener,
                           OnDeleteListener deleteListener) {
        this.categories     = categories;
        this.editListener   = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category cat = categories.get(position);
        holder.tvName.setText(cat.name);

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(cat);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(cat.id, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return categories.size(); }

    public void removeItem(int position) {
        categories.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvCategoryName);
            btnEdit   = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
