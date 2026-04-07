package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AddProductActivity extends AppCompatActivity {

    public static final String EXTRA_NAME        = "name";
    public static final String EXTRA_PRICE       = "price";
    public static final String EXTRA_INGREDIENTS = "ingredients";

    ImageView imgProduct;
    EditText etProductName, etProductPrice, etIngredients;
    TextView btnBack;
    Button btnCreateProduct;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) imgProduct.setImageURI(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        imgProduct      = findViewById(R.id.imgProduct);
        etProductName   = findViewById(R.id.etProductName);
        etProductPrice  = findViewById(R.id.etProductPrice);
        etIngredients   = findViewById(R.id.etIngredients);
        btnBack         = findViewById(R.id.btnBack);
        btnCreateProduct = findViewById(R.id.btnCreateProduct);

        imgProduct.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        btnBack.setOnClickListener(v -> finish());

        btnCreateProduct.setOnClickListener(v -> {
            String name        = etProductName.getText().toString().trim();
            String priceStr    = etProductPrice.getText().toString().trim();
            String ingredients = etIngredients.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Name and price are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent result = new Intent();
            result.putExtra(EXTRA_NAME, name);
            result.putExtra(EXTRA_PRICE, Double.parseDouble(priceStr));
            result.putExtra(EXTRA_INGREDIENTS, ingredients);
            setResult(RESULT_OK, result);
            finish();
        });
    }
}
