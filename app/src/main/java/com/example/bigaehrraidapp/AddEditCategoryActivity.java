package com.example.bigaehrraidapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditCategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID           = "category_id";
    public static final String EXTRA_CATEGORY_NAME         = "category_name";
    public static final String EXTRA_CATEGORY_CANONICAL    = "category_canonical";
    public static final String EXTRA_CATEGORY_SORT_ORDER   = "category_sort_order";

    static final List<String> CANONICAL_TAGS = Arrays.asList(
        "Appetizers & Starters",
        "Main Courses",
        "Beverages & Drinks",
        "Desserts",
        "Side Dishes",
        "Salads",
        "Soups",
        "Breakfast & Brunch",
        "Snacks",
        "Pizza",
        "Burgers",
        "Sandwiches & Wraps",
        "Seafood",
        "Vegetarian",
        "Vegan",
        "Gluten-Free Options",
        "Kids Menu",
        "Combo Meals",
        "Chef's Specials",
        "Other"
    );

    private EditText etName;
    private Spinner spinnerTag;
    private Button btnSave;
    private String categoryId;
    private int sortOrder;
    private CategoryRepository categoryRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_category);

        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        sortOrder  = getIntent().getIntExtra(EXTRA_CATEGORY_SORT_ORDER, 0);

        AuthRepository authRepo = AuthRepository.getInstance(this);
        categoryRepo = CategoryRepository.getInstance(authRepo);

        etName      = findViewById(R.id.etCategoryName);
        spinnerTag  = findViewById(R.id.spinnerCanonicalTag);
        btnSave     = findViewById(R.id.btnSaveCategory);
        TextView tvTitle = findViewById(R.id.tvTitle);

        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CANONICAL_TAGS);
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTag.setAdapter(tagAdapter);

        if (categoryId != null) {
            tvTitle.setText("Edit Category");
            btnSave.setText("Save Changes");
            etName.setText(getIntent().getStringExtra(EXTRA_CATEGORY_NAME));
            String existingTag = getIntent().getStringExtra(EXTRA_CATEGORY_CANONICAL);
            if (existingTag != null) {
                int idx = CANONICAL_TAGS.indexOf(existingTag);
                if (idx >= 0) spinnerTag.setSelection(idx);
            }
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        String canonicalTag = CANONICAL_TAGS.get(spinnerTag.getSelectedItemPosition());

        Map<String, Object> data = new HashMap<>();
        data.put("name",         name);
        data.put("canonicalTag", canonicalTag);
        data.put("sortOrder",    sortOrder);

        String msg = categoryId != null ? "Category updated!" : "Category added!";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();

        if (categoryId != null) {
            categoryRepo.updateCategory(categoryId, data, new CategoryRepository.Callback<Void>() {
                @Override public void onSuccess(Void v) {}
                @Override public void onFailure(String error) {}
            });
        } else {
            data.put("createdAt", System.currentTimeMillis());
            categoryRepo.addCategory(data, new CategoryRepository.Callback<String>() {
                @Override public void onSuccess(String id) {}
                @Override public void onFailure(String error) {}
            });
        }
    }
}
