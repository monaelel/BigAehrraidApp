package com.example.bigaehrraidapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RestaurantSetupActivity extends AppCompatActivity {

    private static final String[] PROVINCES = {
        "Select Province / Territory",
        "Alberta",
        "British Columbia",
        "Manitoba",
        "New Brunswick",
        "Newfoundland and Labrador",
        "Northwest Territories",
        "Nova Scotia",
        "Nunavut",
        "Ontario",
        "Prince Edward Island",
        "Quebec",
        "Saskatchewan",
        "Yukon"
    };

    private static final Pattern POSTAL_CODE_PATTERN =
            Pattern.compile("^[A-Za-z]\\d[A-Za-z]\\s?\\d[A-Za-z]\\d$");

    EditText etName, etPhone, etContactEmail, etStreet, etCity, etPostalCode;
    Spinner  spinnerProvince;
    Button   btnSave;

    String  email, password;
    boolean isExistingAccount;

    AuthRepository       authRepo;
    RestaurantRepository restaurantRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_setup);

        authRepo       = AuthRepository.getInstance(this);
        restaurantRepo = RestaurantRepository.getInstance(authRepo);

        etName         = findViewById(R.id.etSetupName);
        etPhone        = findViewById(R.id.etSetupPhone);
        etContactEmail = findViewById(R.id.etSetupContactEmail);
        etStreet       = findViewById(R.id.etSetupStreet);
        etCity         = findViewById(R.id.etSetupCity);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        etPostalCode   = findViewById(R.id.etSetupPostalCode);
        btnSave        = findViewById(R.id.btnSetupSave);

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, PROVINCES);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        email             = getIntent().getStringExtra("email");
        password          = getIntent().getStringExtra("password");
        isExistingAccount = getIntent().getBooleanExtra("isExistingAccount", false);
        if (email != null) etContactEmail.setText(email);

        findViewById(R.id.btnBack).setOnClickListener(v -> confirmBack());
        btnSave.setOnClickListener(v -> attemptSave());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { confirmBack(); }
        });
    }

    private void confirmBack() {
        if (isExistingAccount) {
            new AlertDialog.Builder(this)
                    .setTitle("Setup Required")
                    .setMessage("You must complete your restaurant profile to use the app. Log out instead?")
                    .setPositiveButton("Stay", null)
                    .setNegativeButton("Log Out", (d, w) -> {
                        authRepo.logout();
                        Intent intent = new Intent(this, CustomerMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Registration")
                    .setMessage("Your account has not been created yet. Go back to the previous step?")
                    .setPositiveButton("Stay", null)
                    .setNegativeButton("Go Back", (d, w) -> finish())
                    .show();
        }
    }

    private void attemptSave() {
        String name         = etName.getText().toString().trim();
        String phone        = etPhone.getText().toString().trim();
        String contactEmail = etContactEmail.getText().toString().trim();
        String street       = etStreet.getText().toString().trim();
        String city         = etCity.getText().toString().trim();
        String postalCode   = etPostalCode.getText().toString().trim().toUpperCase();
        int    provincePos  = spinnerProvince.getSelectedItemPosition();
        String province     = provincePos > 0 ? PROVINCES[provincePos] : "";

        if (name.isEmpty() || phone.isEmpty() || contactEmail.isEmpty() ||
                street.isEmpty() || city.isEmpty() || province.isEmpty() ||
                postalCode.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(contactEmail).matches()) {
            Toast.makeText(this, "Please enter a valid contact email", Toast.LENGTH_SHORT).show();
            return;
        }

        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() != 10) {
            Toast.makeText(this, "Phone number must be exactly 10 digits (e.g. 416-555-1234)", Toast.LENGTH_LONG).show();
            return;
        }

        if (!POSTAL_CODE_PATTERN.matcher(postalCode).matches()) {
            Toast.makeText(this, "Postal code must be in format A1A 1A1", Toast.LENGTH_LONG).show();
            return;
        }

        String normalizedPostal = postalCode.replaceAll("\\s", "");
        String formattedPostal  = normalizedPostal.substring(0, 3) + " " + normalizedPostal.substring(3);
        String formattedPhone   = digitsOnly.substring(0, 3) + "-" + digitsOnly.substring(3, 6) + "-" + digitsOnly.substring(6);

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        restaurantRepo.isPhoneTaken(formattedPhone, new RestaurantRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean taken) {
                if (taken) {
                    resetButton();
                    Toast.makeText(RestaurantSetupActivity.this,
                            "This phone number is already registered to another restaurant",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                checkEmailThenSave(name, formattedPhone, contactEmail, street, city, province, formattedPostal);
            }

            @Override
            public void onFailure(String error) {
                resetButton();
                Toast.makeText(RestaurantSetupActivity.this, "Error checking phone: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkEmailThenSave(String name, String phone, String contactEmail,
                                    String street, String city, String province, String postalCode) {
        restaurantRepo.isContactEmailTaken(contactEmail, new RestaurantRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean taken) {
                if (taken) {
                    resetButton();
                    Toast.makeText(RestaurantSetupActivity.this,
                            "This contact email is already registered to another restaurant",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                saveProfile(name, phone, contactEmail, street, city, province, postalCode);
            }

            @Override
            public void onFailure(String error) {
                resetButton();
                Toast.makeText(RestaurantSetupActivity.this, "Error checking email: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile(String name, String phone, String contactEmail,
                             String street, String city, String province, String postalCode) {
        Map<String, Object> data = new HashMap<>();
        data.put("name",       name);
        data.put("phone",      phone);
        data.put("mail",       contactEmail);
        data.put("street",     street);
        data.put("city",       city);
        data.put("province",   province);
        data.put("postalCode", postalCode);
        data.put("lat",        0.0);
        data.put("lng",        0.0);

        if (isExistingAccount) {
            restaurantRepo.saveProfile(data, new RestaurantRepository.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Intent intent = new Intent(RestaurantSetupActivity.this, RestaurantMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String error) {
                    resetButton();
                    Toast.makeText(RestaurantSetupActivity.this, "Failed to save: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            authRepo.registerRestaurant(email, password, data, new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(RestaurantSetupActivity.this, RestaurantMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String error) {
                    resetButton();
                    Toast.makeText(RestaurantSetupActivity.this, "Failed to create account: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void resetButton() {
        btnSave.setEnabled(true);
        btnSave.setText("Save & Continue");
    }
}
