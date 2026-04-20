package com.example.bigaehrraidapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class AddEditCategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID         = "category_id";
    public static final String EXTRA_CATEGORY_NAME       = "category_name";
    public static final String EXTRA_CATEGORY_SORT_ORDER = "category_sort_order";

    private EditText etName;
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

        etName  = findViewById(R.id.etCategoryName);
        btnSave = findViewById(R.id.btnSaveCategory);
        TextView tvTitle = findViewById(R.id.tvTitle);

        if (categoryId != null) {
            tvTitle.setText("Edit Category");
            btnSave.setText("Save Changes");
            etName.setText(getIntent().getStringExtra(EXTRA_CATEGORY_NAME));
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

        Map<String, Object> data = new HashMap<>();
        data.put("name",      name);
        data.put("sortOrder", sortOrder);

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
