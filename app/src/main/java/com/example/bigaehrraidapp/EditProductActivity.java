package com.example.bigaehrraidapp;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID          = "product_id";
    public static final String EXTRA_PRODUCT_NAME        = "product_name";
    public static final String EXTRA_PRODUCT_DESC        = "product_desc";
    public static final String EXTRA_PRODUCT_PRICE       = "product_price";
    public static final String EXTRA_PRODUCT_STOCK       = "product_stock";
    public static final String EXTRA_PRODUCT_CATEGORY_ID = "product_category_id";
    public static final String EXTRA_PRODUCT_IMAGE       = "product_image";

    private EditText etName, etDescription, etPrice, etStock;
    private Spinner spinnerCategory;
    private ImageView ivProductImage;
    private LinearLayout layoutImagePlaceholder;
    private Button btnSave;
    private Uri selectedImageUri;
    private String productId;
    private String existingImageUrl;
    private ProductRepository productRepo;
    private StorageReference storageRef;
    private List<Category> categories = new ArrayList<>();

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProductImage.setVisibility(View.VISIBLE);
                    layoutImagePlaceholder.setVisibility(View.GONE);
                    Glide.with(this).load(uri).centerCrop().into(ivProductImage);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        if (productId == null) { finish(); return; }

        AuthRepository authRepo = AuthRepository.getInstance(this);
        productRepo = ProductRepository.getInstance(authRepo);
        storageRef  = FirebaseStorage.getInstance().getReference("products");

        etName                 = findViewById(R.id.etProductName);
        etDescription          = findViewById(R.id.etProductDescription);
        etPrice                = findViewById(R.id.etProductPrice);
        etStock                = findViewById(R.id.etProductStock);
        spinnerCategory        = findViewById(R.id.spinnerCategory);
        ivProductImage         = findViewById(R.id.ivProductImage);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);
        btnSave                = findViewById(R.id.btnSaveProduct);

        etName.setText(getIntent().getStringExtra(EXTRA_PRODUCT_NAME));
        etDescription.setText(getIntent().getStringExtra(EXTRA_PRODUCT_DESC));
        String priceStr = getIntent().getStringExtra(EXTRA_PRODUCT_PRICE);
        if (priceStr != null) etPrice.setText(priceStr);
        String stockStr = getIntent().getStringExtra(EXTRA_PRODUCT_STOCK);
        if (stockStr != null) etStock.setText(stockStr);

        existingImageUrl = getIntent().getStringExtra(EXTRA_PRODUCT_IMAGE);
        if (!TextUtils.isEmpty(existingImageUrl)) {
            ivProductImage.setVisibility(View.VISIBLE);
            layoutImagePlaceholder.setVisibility(View.GONE);
            Glide.with(this).load(existingImageUrl).centerCrop().into(ivProductImage);
        }

        FrameLayout layoutImagePicker = findViewById(R.id.layoutImagePicker);
        layoutImagePicker.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveChanges());

        loadCategories();
    }

    private void loadCategories() {
        String incomingCategoryId = getIntent().getStringExtra(EXTRA_PRODUCT_CATEGORY_ID);
        categories = Category.getFixedCategories();
        List<String> names = new ArrayList<>();
        names.add("No category");
        for (Category c : categories) names.add(c.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        if (incomingCategoryId != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).id.equals(incomingCategoryId)) {
                    spinnerCategory.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    private void saveChanges() {
        String name        = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr    = etPrice.getText().toString().trim();
        String stockStr    = etStock.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Product name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Enter a valid price");
            etPrice.requestFocus();
            return;
        }

        int stock = 0;
        if (!TextUtils.isEmpty(stockStr)) {
            try { stock = Integer.parseInt(stockStr); } catch (NumberFormatException ignored) {}
        }

        int selectedPos = spinnerCategory.getSelectedItemPosition();
        String categoryId   = null;
        String categoryName = null;
        if (selectedPos > 0 && selectedPos - 1 < categories.size()) {
            Category cat = categories.get(selectedPos - 1);
            categoryId   = cat.id;
            categoryName = cat.name;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        final int    finalStock        = stock;
        final String finalCategoryId   = categoryId;
        final String finalCategoryName = categoryName;

        if (selectedImageUri != null) {
            String filename = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageRef.child(filename);
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(snap ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                    persist(name, description, price, finalCategoryId, finalCategoryName,
                                            finalStock, uri.toString())))
                    .addOnFailureListener(e -> resetSaveButton());
        } else {
            persist(name, description, price, finalCategoryId, finalCategoryName, finalStock, existingImageUrl);
        }
    }

    private void persist(String name, String description, double price,
                         String categoryId, String categoryName, int stock, String imageUrl) {
        Map<String, Object> data = new HashMap<>();
        data.put("name",        name);
        data.put("description", description);
        data.put("price",       price);
        data.put("stock",       stock);
        data.put("categoryId",   categoryId != null ? categoryId : "");
        data.put("categoryName", categoryName != null ? categoryName : "");
        if (imageUrl != null) data.put("imageUrl", imageUrl);

        productRepo.updateProduct(productId, data, new ProductRepository.Callback<Void>() {
            @Override public void onSuccess(Void v) {
                Toast.makeText(EditProductActivity.this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
            @Override public void onFailure(String error) {
                resetSaveButton();
            }
        });
    }

    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText("Save Changes");
    }
}
