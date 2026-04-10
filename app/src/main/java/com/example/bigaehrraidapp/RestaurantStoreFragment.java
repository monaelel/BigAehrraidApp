package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestaurantStoreFragment extends Fragment implements StoreAdapter.Listener {

    private LinearLayout layoutEmptyState;
    private RecyclerView rvProducts;
    private StoreAdapter storeAdapter;
    private final List<StoreItem> storeItems = new ArrayList<>();

    private ProductRepository productRepo;
    private CategoryRepository categoryRepo;
    private List<Category> categories = new ArrayList<>();
    private List<Map<String, Object>> products = new ArrayList<>();

    private final ActivityResultLauncher<Intent> productFormLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) loadAll();
            });

    private final ActivityResultLauncher<Intent> categoryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) loadAll();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_store, container, false);

        AuthRepository authRepo = AuthRepository.getInstance(requireContext());
        productRepo  = ProductRepository.getInstance(authRepo);
        categoryRepo = CategoryRepository.getInstance(authRepo);

        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        rvProducts       = view.findViewById(R.id.rvProducts);

        storeAdapter = new StoreAdapter(storeItems, this);
        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProducts.setAdapter(storeAdapter);

        view.findViewById(R.id.btnAddProduct).setOnClickListener(v ->
                productFormLauncher.launch(new Intent(requireContext(), AddProductActivity.class)));

        view.findViewById(R.id.btnCategories).setOnClickListener(v ->
                categoryLauncher.launch(new Intent(requireContext(), ManageCategoriesActivity.class)));

        loadAll();
        return view;
    }

    private void loadAll() {
        categoryRepo.loadCategories(new CategoryRepository.Callback<List<Category>>() {
            @Override public void onSuccess(List<Category> data) {
                categories = data;
                loadProducts();
            }
            @Override public void onFailure(String error) {
                loadProducts();
            }
        });
    }

    private void loadProducts() {
        productRepo.loadProducts(new ProductRepository.Callback<List<Map<String, Object>>>() {
            @Override public void onSuccess(List<Map<String, Object>> data) {
                products = data;
                buildStoreItems();
            }
            @Override public void onFailure(String error) {
                Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                buildStoreItems();
            }
        });
    }

    private void buildStoreItems() {
        Map<String, String> categoryNames = new LinkedHashMap<>();
        for (Category cat : categories) categoryNames.put(cat.id, cat.name);

        LinkedHashMap<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Category cat : categories) grouped.put(cat.id, new ArrayList<>());

        List<Map<String, Object>> uncategorized = new ArrayList<>();
        for (Map<String, Object> product : products) {
            String catId = (String) product.get("categoryId");
            if (catId != null && grouped.containsKey(catId)) {
                grouped.get(catId).add(product);
            } else {
                uncategorized.add(product);
            }
        }

        storeItems.clear();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            List<Map<String, Object>> prods = entry.getValue();
            if (prods.isEmpty()) continue;
            storeItems.add(StoreItem.header(categoryNames.get(entry.getKey())));
            for (Map<String, Object> p : prods) storeItems.add(StoreItem.product(p));
        }
        if (!uncategorized.isEmpty()) {
            storeItems.add(StoreItem.header("Uncategorized"));
            for (Map<String, Object> p : uncategorized) storeItems.add(StoreItem.product(p));
        }

        storeAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = products.isEmpty();
        layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDelete(String productId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) ->
                        productRepo.deleteProduct(productId, new ProductRepository.Callback<Void>() {
                            @Override public void onSuccess(Void v) { loadAll(); }
                            @Override public void onFailure(String error) {
                                Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEdit(Map<String, Object> product) {
        Object priceObj = product.get("price");
        Object stockObj = product.get("stock");
        String priceStr = priceObj instanceof Number ? String.valueOf(((Number) priceObj).doubleValue()) : "0.0";
        String stockStr = stockObj instanceof Number ? String.valueOf(((Number) stockObj).intValue()) : "";

        Intent intent = new Intent(requireContext(), EditProductActivity.class);
        intent.putExtra(EditProductActivity.EXTRA_PRODUCT_ID,          (String) product.get("productId"));
        intent.putExtra(EditProductActivity.EXTRA_PRODUCT_NAME,        (String) product.getOrDefault("name", ""));
        intent.putExtra(EditProductActivity.EXTRA_PRODUCT_DESC,        (String) product.getOrDefault("description", ""));
        intent.putExtra(EditProductActivity.EXTRA_PRODUCT_PRICE,       priceStr);
        intent.putExtra(EditProductActivity.EXTRA_PRODUCT_STOCK,       stockStr);
        intent.putExtra(EditProductActivity.EXTRA_PRODUCT_CATEGORY_ID, (String) product.get("categoryId"));
        intent.putExtra(EditProductActivity.EXTRA_PRODUCT_IMAGE,       (String) product.get("imageUrl"));
        productFormLauncher.launch(intent);
    }

    @Override
    public void onToggle(String productId, boolean newAvailable, Map<String, Object> productData, int flatPosition) {
        Map<String, Object> update = new java.util.HashMap<>();
        update.put("available", newAvailable);
        productRepo.updateProduct(productId, update, new ProductRepository.Callback<Void>() {
            @Override public void onSuccess(Void v) {}
            @Override public void onFailure(String error) {
                productData.put("available", !newAvailable);
                storeAdapter.notifyItemChanged(flatPosition);
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
