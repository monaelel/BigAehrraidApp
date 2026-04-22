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

public class AddProductActivity extends AppCompatActivity {

    private EditText etName, etDescription, etPrice, etStock;
    private Spinner spinnerCategory;
    private ImageView ivProductImage;
    private LinearLayout layoutImagePlaceholder;
    private Button btnSave;
    private Uri selectedImageUri;
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
        setContentView(R.layout.activity_add_product);

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

        FrameLayout layoutImagePicker = findViewById(R.id.layoutImagePicker);
        layoutImagePicker.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveProduct());

        loadCategories();
    }

    private void loadCategories() {
        categories = Category.getFixedCategories();
        List<String> names = new ArrayList<>();
        names.add("No category");
        for (Category c : categories) names.add(c.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void saveProduct() {
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
            persist(name, description, price, finalCategoryId, finalCategoryName, finalStock, null);
        }
    }

    private void persist(String name, String description, double price,
                         String categoryId, String categoryName, int stock, String imageUrl) {
        Map<String, Object> product = new HashMap<>();
        product.put("name",         name);
        product.put("description",  description);
        product.put("price",        price);
        product.put("stock",        stock);
        product.put("available",    true);
        product.put("createdAt",    System.currentTimeMillis());
        if (categoryId   != null) product.put("categoryId",   categoryId);
        if (categoryName != null) product.put("categoryName", categoryName);
        if (imageUrl     != null) product.put("imageUrl",     imageUrl);

        productRepo.addProduct(product, new ProductRepository.Callback<String>() {
            @Override public void onSuccess(String id) {
                Toast.makeText(AddProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();
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
        btnSave.setText("Save Product");
    }
}
