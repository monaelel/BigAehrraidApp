package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RestaurantStoreFragment extends Fragment {

    RecyclerView rvProducts;
    View btnAddProduct;
    List<ProductStock> products;
    ProductStockAdapter adapter;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    int pos     = data.getIntExtra(EditProductActivity.EXTRA_POSITION, -1);
                    String name = data.getStringExtra(EditProductActivity.EXTRA_NAME);
                    double price = data.getDoubleExtra(EditProductActivity.EXTRA_PRICE, 0);
                    String ingr = data.getStringExtra(EditProductActivity.EXTRA_INGREDIENTS);

                    if (pos >= 0 && pos < products.size()) {
                        ProductStock p = products.get(pos);
                        p.name = name;
                        p.pricePerUnit = price;
                        p.ingredients = ingr;
                        adapter.notifyItemChanged(pos);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> addLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String name  = data.getStringExtra(AddProductActivity.EXTRA_NAME);
                    double price = data.getDoubleExtra(AddProductActivity.EXTRA_PRICE, 0);
                    String ingr  = data.getStringExtra(AddProductActivity.EXTRA_INGREDIENTS);

                    products.add(new ProductStock(name, 0, price, ingr));
                    adapter.notifyItemInserted(products.size() - 1);
                    rvProducts.scrollToPosition(products.size() - 1);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_store, container, false);

        rvProducts    = view.findViewById(R.id.rvProducts);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);

        products = new ArrayList<>();
        products.add(new ProductStock("Burger Patty",   50,  3.99, "• Beef\n• Salt\n• Pepper"));
        products.add(new ProductStock("Sesame Bun",     120, 0.75, "• Flour\n• Sesame seeds\n• Yeast"));
        products.add(new ProductStock("Cheddar Cheese", 80,  1.25, "• Milk\n• Salt\n• Rennet"));

        adapter = new ProductStockAdapter(products, new ProductStockAdapter.OnItemActionListener() {
            @Override
            public void onDelete(int position) {
                products.remove(position);
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onEdit(int position) {
                ProductStock p = products.get(position);
                Intent intent = new Intent(getContext(), EditProductActivity.class);
                intent.putExtra(EditProductActivity.EXTRA_POSITION,    position);
                intent.putExtra(EditProductActivity.EXTRA_NAME,        p.name);
                intent.putExtra(EditProductActivity.EXTRA_PRICE,       p.pricePerUnit);
                intent.putExtra(EditProductActivity.EXTRA_STOCKS,      p.stockCount);
                intent.putExtra(EditProductActivity.EXTRA_INGREDIENTS, p.ingredients);
                editLauncher.launch(intent);
            }
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        btnAddProduct.setOnClickListener(v ->
                addLauncher.launch(new Intent(getContext(), AddProductActivity.class))
        );

        return view;
    }
}
