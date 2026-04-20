package com.example.bigaehrraidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomerRestaurantMenuActivity extends AppCompatActivity implements CartManager.OnCartChangeListener {

    public static final String EXTRA_RESTAURANT_ID   = "restaurant_id";
    public static final String EXTRA_RESTAURANT_NAME = "restaurant_name";

    private final List<StoreItem> storeItems = new ArrayList<>();
    private CustomerMenuAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private android.widget.Button btnViewCart;

    private List<Category> categories = new ArrayList<>();
    private List<Map<String, Object>> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_restaurant_menu);

        String restaurantId   = getIntent().getStringExtra(EXTRA_RESTAURANT_ID);
        String restaurantName = getIntent().getStringExtra(EXTRA_RESTAURANT_NAME);

        TextView tvName = findViewById(R.id.tvRestaurantName);
        progressBar     = findViewById(R.id.progressBar);
        tvEmpty         = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.rvMenu);
        btnViewCart     = findViewById(R.id.btnViewCart);

        tvName.setText(restaurantName != null ? restaurantName : "Menu");
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new CustomerMenuAdapter(storeItems);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnViewCart.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, CartActivity.class);
            startActivity(intent);
        });

        CartManager.getInstance().setListener(this);
        updateCartTotal();

        if (restaurantId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loadCategories(restaurantId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartManager.getInstance().setListener(null);
    }

    @Override
    public void onCartChanged() {
        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = CartManager.getInstance().getSubtotal();
        btnViewCart.setText(String.format(java.util.Locale.getDefault(), "View Cart ($%.2f)", total));
        if (total > 0) {
            btnViewCart.setVisibility(View.VISIBLE);
        } else {
            btnViewCart.setVisibility(View.GONE);
        }
    }

    private void loadCategories(String restaurantId) {
        FirebaseFirestore.getInstance()
            .collection("restaurants").document(restaurantId)
            .collection("categories")
            .orderBy("sortOrder")
            .get()
            .addOnSuccessListener(snaps -> {
                for (QueryDocumentSnapshot doc : snaps) {
                    Category cat = new Category();
                    cat.id   = doc.getId();
                    cat.name = doc.getString("name");
                    Long so  = doc.getLong("sortOrder");
                    cat.sortOrder   = so != null ? so.intValue() : 0;
                    categories.add(cat);
                }
                loadProducts(restaurantId);
            })
            .addOnFailureListener(e -> loadProducts(restaurantId));
    }

    private void loadProducts(String restaurantId) {
        FirebaseFirestore.getInstance()
            .collection("restaurants").document(restaurantId)
            .collection("products")
            .get()
            .addOnSuccessListener(snaps -> {
                for (QueryDocumentSnapshot doc : snaps) {
                    Map<String, Object> data = doc.getData();
                    data.put("productId", doc.getId());
                    products.add(data);
                }
                buildMenu();
            })
            .addOnFailureListener(e -> buildMenu());
    }

    private void buildMenu() {
        progressBar.setVisibility(View.GONE);

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
            storeItems.add(StoreItem.header("Other"));
            for (Map<String, Object> p : uncategorized) storeItems.add(StoreItem.product(p));
        }

        adapter.notifyDataSetChanged();

        if (storeItems.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }
}
