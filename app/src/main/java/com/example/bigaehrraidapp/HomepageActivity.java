package com.example.bigaehrraidapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomepageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        findViewById(R.id.btnHomeLogin).setOnClickListener(v -> 
            startActivity(new android.content.Intent(this, RoleSelectionActivity.class))
        );

        findViewById(R.id.btnHomeRegister).setOnClickListener(v -> 
            startActivity(new android.content.Intent(this, RoleSelectionActivity.class))
        );

    }
}