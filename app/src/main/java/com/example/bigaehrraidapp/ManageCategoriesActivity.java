package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity {

    private LinearLayout layoutEmpty;
    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private final List<Category> categoryList = new ArrayList<>();
    private CategoryRepository categoryRepo;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadCategories();
                    setResult(RESULT_OK);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        AuthRepository authRepo = AuthRepository.getInstance(this);
        categoryRepo = CategoryRepository.getInstance(authRepo);

        layoutEmpty   = findViewById(R.id.layoutEmptyCategories);
        rvCategories  = findViewById(R.id.rvCategories);

        adapter = new CategoryAdapter(categoryList, this::openEdit, this::confirmDelete);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnAdd = findViewById(R.id.btnAddCategory);
        btnAdd.setOnClickListener(v -> openAdd());

        loadCategories();
    }

    private void openAdd() {
        Intent intent = new Intent(this, AddEditCategoryActivity.class);
        intent.putExtra(AddEditCategoryActivity.EXTRA_CATEGORY_SORT_ORDER, categoryList.size());
        formLauncher.launch(intent);
    }

    private void openEdit(Category category) {
        Intent intent = new Intent(this, AddEditCategoryActivity.class);
        intent.putExtra(AddEditCategoryActivity.EXTRA_CATEGORY_ID,         category.id);
        intent.putExtra(AddEditCategoryActivity.EXTRA_CATEGORY_NAME,       category.name);
        intent.putExtra(AddEditCategoryActivity.EXTRA_CATEGORY_SORT_ORDER, category.sortOrder);
        formLauncher.launch(intent);
    }

    private void confirmDelete(String categoryId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Delete this category? Products in it will become uncategorized.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    categoryRepo.deleteCategory(categoryId, new CategoryRepository.Callback<Void>() {
                        @Override public void onSuccess(Void v) {
                            adapter.removeItem(position);
                            updateEmptyState();
                            setResult(RESULT_OK);
                        }
                        @Override public void onFailure(String error) {
                            Toast.makeText(ManageCategoriesActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadCategories() {
        categoryRepo.loadCategories(new CategoryRepository.Callback<List<Category>>() {
            @Override public void onSuccess(List<Category> data) {
                categoryList.clear();
                categoryList.addAll(data);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }
            @Override public void onFailure(String error) {
                Toast.makeText(ManageCategoriesActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (categoryList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
        }
    }
}
