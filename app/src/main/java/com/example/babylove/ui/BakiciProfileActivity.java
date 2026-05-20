package com.example.babylove.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.babylove.R;
import com.example.babylove.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.babylove.utils.ImageStorageHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BakiciProfileActivity extends AppCompatActivity {

    private EditText etCity, etAge, etExperience, etWorkingHours;
    private Spinner spinnerGender;
    private ImageView ivProfileImage;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String bakiciId;
    private Uri selectedImageUri;
    private String currentImageUrl = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfileImage.setPadding(0, 0, 0, 0);
                    Glide.with(this).load(selectedImageUri).circleCrop().into(ivProfileImage);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bakici_profile);

        db = FirebaseFirestore.getInstance();
        try {
            storage = FirebaseStorage.getInstance("gs://babylovee-75a94.firebasestorage.app");
        } catch (Exception e) {
            storage = FirebaseStorage.getInstance();
        }

        SharedPreferences prefs = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE);
        bakiciId = prefs.getString("username", null);

        MaterialToolbar toolbar = findViewById(R.id.toolbarProfile);
        toolbar.setNavigationOnClickListener(v -> finish());

        etCity = findViewById(R.id.etCity);
        etAge = findViewById(R.id.etAge);
        etExperience = findViewById(R.id.etExperience);
        etWorkingHours = findViewById(R.id.etWorkingHours);
        spinnerGender = findViewById(R.id.spinnerGender);
        ivProfileImage = findViewById(R.id.ivProfileImage);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Cinsiyet Seçiniz", "Kadın", "Erkek"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        FloatingActionButton fabAddPhoto = findViewById(R.id.fabAddPhoto);
        fabAddPhoto.setOnClickListener(v -> openImagePicker());

        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        loadProfileData();
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void loadProfileData() {
        if (bakiciId == null) return;

        db.collection("users").document(bakiciId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            if (user.getCity() != null) etCity.setText(user.getCity());
                            if (user.getAge() > 0) etAge.setText(String.valueOf(user.getAge()));
                            if (user.getExperienceYears() > 0) etExperience.setText(String.valueOf(user.getExperienceYears()));
                            if (user.getWorkingHours() != null) etWorkingHours.setText(user.getWorkingHours());

                            if (user.getGender() != null) {
                                if (user.getGender().equals("Kadın")) spinnerGender.setSelection(1);
                                else if (user.getGender().equals("Erkek")) spinnerGender.setSelection(2);
                            }

                            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                currentImageUrl = user.getProfileImageUrl();
                                ivProfileImage.setPadding(0, 0, 0, 0);
                                Glide.with(this).load(currentImageUrl).circleCrop().into(ivProfileImage);
                            }
                        }
                    }
                });
    }

    private void saveProfile() {
        String city = etCity.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String expStr = etExperience.getText().toString().trim();
        String hours = etWorkingHours.getText().toString().trim();
        int genderIndex = spinnerGender.getSelectedItemPosition();

        if (city.isEmpty() || ageStr.isEmpty() || expStr.isEmpty() || hours.isEmpty() || genderIndex == 0) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        int exp = Integer.parseInt(expStr);
        String gender = genderIndex == 1 ? "Kadın" : "Erkek";

        if (selectedImageUri != null) {
            
            String localPath = ImageStorageHelper.saveImageToInternalStorage(this, selectedImageUri, "profiles");
            if (localPath != null) {
                updateUserData(city, age, exp, hours, gender, localPath);
            } else {
                Toast.makeText(this, "Profil fotoğrafı kaydedilemedi.", Toast.LENGTH_SHORT).show();
            }
        } else {
            updateUserData(city, age, exp, hours, gender, currentImageUrl);
        }
    }

    private void updateUserData(String city, int age, int exp, String hours, String gender, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("city", city);
        updates.put("age", age);
        updates.put("experienceYears", exp);
        updates.put("workingHours", hours);
        updates.put("gender", gender);
        if (imageUrl != null) {
            updates.put("profileImageUrl", imageUrl);
        }

        db.collection("users").document(bakiciId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil güncellendi!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
