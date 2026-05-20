package com.example.babylove.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.babylove.R;
import com.example.babylove.adapters.LogEntryAdapter;
import com.example.babylove.adapters.WardrobeAdapter;
import com.example.babylove.models.LogEntry;
import com.example.babylove.models.WardrobeItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.babylove.utils.ImageStorageHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class VeliPanelActivity extends AppCompatActivity {

    private EditText etClothesName, etTags;
    private ImageView ivPreview;
    private RecyclerView rvWardrobe, rvLogs;
    private TextView tvWardrobeEmpty, tvLogsEmpty;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private WardrobeAdapter wardrobeAdapter;
    private LogEntryAdapter logEntryAdapter;
    private Uri selectedImageUri;
    private String familyId;
    private boolean isFirstLoadLogs = true;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivPreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).centerCrop().into(ivPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veli_panel);

        db = FirebaseFirestore.getInstance();
        try {
            storage = FirebaseStorage.getInstance("gs://babylovee-75a94.firebasestorage.app");
        } catch (Exception e) {
            storage = FirebaseStorage.getInstance();
        }

        familyId = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE).getString("familyId", "default");

        
        MaterialToolbar toolbar = findViewById(R.id.toolbarVeli);
        toolbar.setNavigationOnClickListener(v -> logout());

        
        Button btnSelectBakiciToolbar = findViewById(R.id.btnSelectBakiciToolbar);
        btnSelectBakiciToolbar.setOnClickListener(v -> {
            Intent intent = new Intent(this, BakiciSecActivity.class);
            startActivity(intent);
        });

        
        etClothesName = findViewById(R.id.etClothesName);
        etTags = findViewById(R.id.etTags);
        ivPreview = findViewById(R.id.ivPreview);
        Button btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        Button btnEkle = findViewById(R.id.btnEkle);
        Button btnCalendar = findViewById(R.id.btnCalendar);
        Button btnToggleWardrobe = findViewById(R.id.btnToggleWardrobe);
        View layoutWardrobeContainer = findViewById(R.id.layoutWardrobeContainer);
        tvWardrobeEmpty = findViewById(R.id.tvWardrobeEmpty);
        tvLogsEmpty = findViewById(R.id.tvLogsEmpty);

        
        btnToggleWardrobe.setOnClickListener(v -> {
            if (layoutWardrobeContainer.getVisibility() == View.VISIBLE) {
                layoutWardrobeContainer.setVisibility(View.GONE);
            } else {
                layoutWardrobeContainer.setVisibility(View.VISIBLE);
            }
        });

        
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
        });

        
        Button btnGrowthCharts = findViewById(R.id.btnGrowthCharts);
        btnGrowthCharts.setOnClickListener(v -> {
            Intent intent = new Intent(this, GrowthChartActivity.class);
            startActivity(intent);
        });

        
        Button btnChat = findViewById(R.id.btnChat);
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("role", "veli");
            startActivity(intent);
        });

        
        btnSelectPhoto.setOnClickListener(v -> openImagePicker());

        
        btnEkle.setOnClickListener(v -> saveWardrobeItem());

        
        rvWardrobe = findViewById(R.id.rvWardrobe);
        rvWardrobe.setLayoutManager(new GridLayoutManager(this, 2));
        wardrobeAdapter = new WardrobeAdapter();
        wardrobeAdapter.setOnItemClickListener((item, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Kıyafeti Sil")
                    .setMessage("\"" + (item.getName() != null ? item.getName() : "Kıyafet") + "\" silinsin mi?")
                    .setPositiveButton("Sil", (d, w) -> deleteWardrobeItem(item, position))
                    .setNegativeButton("İptal", null)
                    .show();
        });
        rvWardrobe.setAdapter(wardrobeAdapter);

        
        rvLogs = findViewById(R.id.rvLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        logEntryAdapter = new LogEntryAdapter();
        rvLogs.setAdapter(logEntryAdapter);

        
        loadWardrobe();
        loadLogs();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logout();
    }

    private void logout() {
        getSharedPreferences("BabyLovePrefs", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openImagePicker() {
        String[] options = {
                "Kışlık Kalın Mont",
                "İnce Yazlık Tişört",
                "Mevsimlik Hırka",
                "Yağmurluk",
                "Rüzgarlık",
                "Kalın Pantolon",
                "Yazlık Şort",
                "Galeriden Seç..."
        };

        String[] drawables = {
                "ic_mont",
                "ic_tisort",
                "ic_hirka",
                "ic_yagmurluk",
                "ic_ruzgarlik",
                "ic_pantolon",
                "ic_sort"
        };

        new AlertDialog.Builder(this)
                .setTitle("Kıyafet Görseli Seçin")
                .setItems(options, (dialog, which) -> {
                    if (which == options.length - 1) {
                        
                        imagePickerLauncher.launch("image/*");
                    } else {
                        
                        String selectedResName = drawables[which];
                        selectedImageUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/" + selectedResName);

                        ivPreview.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(ivPreview);

                        
                        if (etClothesName.getText().toString().trim().isEmpty()) {
                            etClothesName.setText(options[which]);
                        }
                        if (etTags.getText().toString().trim().isEmpty()) {
                            if (which == 0) etTags.setText("kışlık, kalın, mont");
                            else if (which == 1) etTags.setText("yazlık, ince, tişört");
                            else if (which == 2) etTags.setText("mevsimlik, hırka");
                            else if (which == 3) etTags.setText("mevsimlik, yağmurluk, su geçirmez");
                            else if (which == 4) etTags.setText("mevsimlik, rüzgarlık");
                            else if (which == 5) etTags.setText("kışlık, kalın, pantolon");
                            else if (which == 6) etTags.setText("yazlık, ince, şort");
                        }
                    }
                })
                .show();
    }

    private void saveWardrobeItem() {
        String tagsInput = etTags.getText().toString().trim();
        if (tagsInput.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_tags), Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etClothesName.getText().toString().trim();
        
        String[] tagArray = tagsInput.split("[,\\s]+");
        List<String> tags = new ArrayList<>();
        java.util.Locale trLocale = new java.util.Locale("tr", "TR");
        for (String tag : tagArray) {
            String trimmed = tag.trim().toLowerCase(trLocale);
            if (!trimmed.isEmpty()) {
                tags.add(trimmed);
                
                if (trimmed.equals("kislik")) tags.add("kışlık");
                else if (trimmed.equals("kışlık")) tags.add("kislik");
                else if (trimmed.equals("yazlik")) tags.add("yazlık");
                else if (trimmed.equals("yazlık")) tags.add("yazlik");
                else if (trimmed.equals("ruzgarlik")) tags.add("rüzgarlık");
                else if (trimmed.equals("rüzgarlık")) tags.add("ruzgarlik");
                else if (trimmed.equals("yagmurluk")) tags.add("yağmurluk");
                else if (trimmed.equals("yağmurluk")) tags.add("yagmurluk");
                else if (trimmed.equals("sort")) tags.add("şort");
                else if (trimmed.equals("şort")) tags.add("sort");
                else if (trimmed.equals("kalin")) tags.add("kalın");
                else if (trimmed.equals("kalın")) tags.add("kalin");
                else if (trimmed.equals("tisort")) tags.add("tişört");
                else if (trimmed.equals("tişört")) tags.add("tisort");
            }
        }

        String id = UUID.randomUUID().toString();

        if (selectedImageUri != null) {
            if ("android.resource".equals(selectedImageUri.getScheme())) {
                
                WardrobeItem item = new WardrobeItem(id, name, selectedImageUri.toString(), tags);
                saveToFirestore(item);
            } else {
                
                String localPath = ImageStorageHelper.saveImageToInternalStorage(this, selectedImageUri, "wardrobe");
                if (localPath != null) {
                    WardrobeItem item = new WardrobeItem(id, name, localPath, tags);
                    saveToFirestore(item);
                } else {
                    Toast.makeText(this, "Görsel yerel hafızaya kaydedilemedi.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            
            WardrobeItem item = new WardrobeItem(id, name, "", tags);
            saveToFirestore(item);
        }
    }

    private void saveToFirestore(WardrobeItem item) {
        db.collection("families").document(familyId).collection("wardrobe").document(item.getId())
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.success_wardrobe_added), Toast.LENGTH_SHORT).show();
                    etClothesName.setText("");
                    etTags.setText("");
                    ivPreview.setVisibility(View.GONE);
                    selectedImageUri = null;
                    loadWardrobe();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteWardrobeItem(WardrobeItem item, int position) {
        db.collection("families").document(familyId).collection("wardrobe").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    wardrobeAdapter.removeItem(position);
                    Toast.makeText(this, "Kıyafet silindi", Toast.LENGTH_SHORT).show();
                    checkWardrobeEmpty();
                });
    }

    private void loadWardrobe() {
        db.collection("families").document(familyId).collection("wardrobe")
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<WardrobeItem> items = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            WardrobeItem item = doc.toObject(WardrobeItem.class);
                            items.add(item);
                        }
                        wardrobeAdapter.setItems(items);
                        checkWardrobeEmpty();
                    }
                });
    }

    private void loadLogs() {
        db.collection("families").document(familyId).collection("logs")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        
                        if (!isFirstLoadLogs && !value.getDocumentChanges().isEmpty()) {
                            for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                                if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    Toast.makeText(this, "📢 Yeni Günlük Kaydı Eklendi!", Toast.LENGTH_LONG).show();
                                    break;
                                }
                            }
                        }
                        isFirstLoadLogs = false;

                        List<LogEntry> entries = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            LogEntry entry = doc.toObject(LogEntry.class);
                            entries.add(entry);
                        }
                        logEntryAdapter.setEntries(entries);
                        tvLogsEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
                        rvLogs.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
    }

    private void checkWardrobeEmpty() {
        boolean empty = wardrobeAdapter.getItemCount() == 0;
        tvWardrobeEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvWardrobe.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
