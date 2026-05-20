package com.example.babylove.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.babylove.R;
import com.example.babylove.adapters.RecommendationAdapter;
import com.example.babylove.models.LogEntry;
import com.example.babylove.models.MilestoneTags;
import com.example.babylove.models.WardrobeItem;
import com.example.babylove.network.WeatherApiService;
import com.example.babylove.network.WeatherResponse;
import com.example.babylove.utils.WeatherRecommender;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.babylove.utils.ImageStorageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BakiciPanelActivity extends AppCompatActivity {

    private static final String API_KEY = "0e7d93a03007010369c1d80f2ad38b30";
    private static final String BASE_URL = "https://api.openweathermap.org/";
    private static final String CITY = "Istanbul";

    
    private ImageView ivWeatherIcon;
    private TextView tvTemperature, tvWeatherDesc, tvWind, tvHumidity, tvCityName;
    private TextView tvRecommendationSummary;

    
    private TextView tvWearTitle, tvDontWearTitle;
    private RecyclerView rvWearThese, rvDontWearThese;
    private RecommendationAdapter wearAdapter, dontWearAdapter;

    
    private EditText etLogTitle, etComment, etHeight, etWeight;
    private ImageView ivLogPreview;
    private ChipGroup chipGroupMilestones;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private List<WardrobeItem> wardrobe = new ArrayList<>();
    private List<String> selectedMilestones = new ArrayList<>();
    private Uri selectedLogImageUri;
    private String familyId;
    private ListenerRegistration relationshipListener;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final ActivityResultLauncher<String> logImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedLogImageUri = uri;
                    ivLogPreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedLogImageUri).centerCrop().into(ivLogPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bakici_panel);

        db = FirebaseFirestore.getInstance();
        try {
            storage = FirebaseStorage.getInstance("gs://babylovee-75a94.firebasestorage.app");
        } catch (Exception e) {
            storage = FirebaseStorage.getInstance();
        }

        familyId = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE).getString("familyId", "default");

        
        MaterialToolbar toolbar = findViewById(R.id.toolbarBakici);
        toolbar.setNavigationOnClickListener(v -> finish());

        
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
        tvWind = findViewById(R.id.tvWind);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvCityName = findViewById(R.id.tvCityName);
        tvRecommendationSummary = findViewById(R.id.tvRecommendationSummary);

        
        tvWearTitle = findViewById(R.id.tvWearTitle);
        tvDontWearTitle = findViewById(R.id.tvDontWearTitle);
        rvWearThese = findViewById(R.id.rvWearThese);
        rvDontWearThese = findViewById(R.id.rvDontWearThese);

        wearAdapter = new RecommendationAdapter();
        dontWearAdapter = new RecommendationAdapter();
        rvWearThese.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDontWearThese.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvWearThese.setAdapter(wearAdapter);
        rvDontWearThese.setAdapter(dontWearAdapter);

        
        etLogTitle = findViewById(R.id.etLogTitle);
        etComment = findViewById(R.id.etComment);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        ivLogPreview = findViewById(R.id.ivLogPreview);
        chipGroupMilestones = findViewById(R.id.chipGroupMilestones);
        Button btnLogPhoto = findViewById(R.id.btnLogPhoto);
        Button btnLog = findViewById(R.id.btnLog);
        Button btnCalendar = findViewById(R.id.btnCalendar);
        Button btnGrowthCharts = findViewById(R.id.btnGrowthCharts);

        
        Button btnQuickFeed = findViewById(R.id.btnQuickFeed);
        Button btnQuickSleep = findViewById(R.id.btnQuickSleep);
        Button btnQuickDiaper = findViewById(R.id.btnQuickDiaper);

        btnQuickFeed.setOnClickListener(v -> saveQuickLog("🍼 Beslenme", "Bebek beslendi.", "beslenme"));
        btnQuickSleep.setOnClickListener(v -> saveQuickLog("😴 Uyku", "Bebek uykuya yattı.", "uyku"));
        btnQuickDiaper.setOnClickListener(v -> saveQuickLog("👶 Bez Değişimi", "Bebeğin altı temizlendi.", "alt_degisimi"));

        
        setupMilestoneChips();

        
        btnLogPhoto.setOnClickListener(v -> openLogImagePicker());
        btnLog.setOnClickListener(v -> saveLogEntry());
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
        });
        btnGrowthCharts.setOnClickListener(v -> {
            Intent intent = new Intent(this, GrowthChartActivity.class);
            startActivity(intent);
        });
        Button btnChat = findViewById(R.id.btnChat);
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("role", "bakici");
            startActivity(intent);
        });

        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        
        loadWardrobeAndWeather();

        
        checkRelationshipStatus();
    }

    private void setupMilestoneChips() {
        for (int i = 0; i < MilestoneTags.PREDEFINED.size(); i++) {
            String tag = MilestoneTags.PREDEFINED.get(i);
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setChecked(false);
            chip.setChipBackgroundColor(ColorStateList.valueOf(
                    MilestoneTags.getColorForIndex(i)));
            chip.setTextSize(13);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedMilestones.add(tag);
                } else {
                    selectedMilestones.remove(tag);
                }
            });

            chipGroupMilestones.addView(chip);
        }
    }

    private void openLogImagePicker() {
        logImageLauncher.launch("image/*");
    }

    private void loadWardrobeAndWeather() {
        db.collection("families").document(familyId).collection("wardrobe").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                wardrobe.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    WardrobeItem item = doc.toObject(WardrobeItem.class);
                    wardrobe.add(item);
                }
                fetchWeatherData();
            } else {
                Toast.makeText(this, "Kıyafetler yüklenemedi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeatherData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            getWeatherByLocation(location.getLatitude(), location.getLongitude());
                        } else {
                            getWeatherByCity(CITY);
                        }
                    })
                    .addOnFailureListener(this, e -> getWeatherByCity(CITY));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchWeatherData(); 
            } else {
                getWeatherByCity(CITY); 
            }
        }
    }

    private void getWeatherByLocation(double lat, double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService api = retrofit.create(WeatherApiService.class);
        api.getCurrentWeatherByCoords(lat, lon, API_KEY, "metric", "tr")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateWeatherUI(response.body());
                            updateRecommendations(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        tvWeatherDesc.setText("Hava durumu alınamadı");
                    }
                });
    }

    private void getWeatherByCity(String cityName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService api = retrofit.create(WeatherApiService.class);
        api.getCurrentWeather(cityName, API_KEY, "metric", "tr")
                .enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateWeatherUI(response.body());
                    updateRecommendations(response.body());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvWeatherDesc.setText("Hava durumu alınamadı");
            }
        });
    }

    private void updateWeatherUI(WeatherResponse weather) {
        tvTemperature.setText(String.format("%.0f°C", weather.main.temp));
        tvWeatherDesc.setText(weather.getDescription());
        tvWind.setText(String.format("💨 %.1f m/s", weather.wind.speed));
        tvHumidity.setText(String.format("💧 %d%%", weather.main.humidity));
        tvCityName.setText("📍 " + (weather.cityName != null ? weather.cityName : CITY));

        
        String iconUrl = weather.getIconUrl();
        if (!iconUrl.isEmpty()) {
            Glide.with(this).load(iconUrl).into(ivWeatherIcon);
        }
    }

    private void updateRecommendations(WeatherResponse weather) {
        WeatherRecommender.RecommendationResult result =
                WeatherRecommender.recommend(weather, wardrobe);

        
        tvRecommendationSummary.setText(result.summary);

        
        if (!result.wearThese.isEmpty()) {
            tvWearTitle.setVisibility(View.VISIBLE);
            rvWearThese.setVisibility(View.VISIBLE);
            wearAdapter.setItems(result.wearThese);
        } else {
            tvWearTitle.setVisibility(View.GONE);
            rvWearThese.setVisibility(View.GONE);
        }

        
        if (!result.dontWearThese.isEmpty()) {
            tvDontWearTitle.setVisibility(View.VISIBLE);
            rvDontWearThese.setVisibility(View.VISIBLE);
            dontWearAdapter.setItems(result.dontWearThese);
        } else {
            tvDontWearTitle.setVisibility(View.GONE);
            rvDontWearThese.setVisibility(View.GONE);
        }
    }

    private void saveLogEntry() {
        String title = etLogTitle.getText().toString().trim();
        String notes = etComment.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (notes.isEmpty() && title.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_comment), Toast.LENGTH_SHORT).show();
            return;
        }

        Double height = heightStr.isEmpty() ? null : Double.parseDouble(heightStr);
        Double weight = weightStr.isEmpty() ? null : Double.parseDouble(weightStr);

        String id = UUID.randomUUID().toString();

        if (selectedLogImageUri != null) {
            
            String localPath = ImageStorageHelper.saveImageToInternalStorage(this, selectedLogImageUri, "logs");
            if (localPath != null) {
                List<String> imageUrls = new ArrayList<>();
                imageUrls.add(localPath);
                LogEntry entry = new LogEntry(id, title, notes, imageUrls,
                        new ArrayList<>(selectedMilestones), System.currentTimeMillis(), height, weight);
                saveLogToFirestore(entry);
            } else {
                Toast.makeText(this, "Görsel yerel hafızaya kaydedilemedi.", Toast.LENGTH_SHORT).show();
            }
        } else {
            LogEntry entry = new LogEntry(id, title, notes, new ArrayList<>(),
                    new ArrayList<>(selectedMilestones), System.currentTimeMillis(), height, weight);
            saveLogToFirestore(entry);
        }
    }

    private void saveLogToFirestore(LogEntry entry) {
        db.collection("families").document(familyId).collection("logs").document(entry.getId())
                .set(entry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.success_log_saved), Toast.LENGTH_SHORT).show();
                    etLogTitle.setText("");
                    etComment.setText("");
                    etHeight.setText("");
                    etWeight.setText("");
                    ivLogPreview.setVisibility(View.GONE);
                    selectedLogImageUri = null;
                    selectedMilestones.clear();
                    
                    for (int i = 0; i < chipGroupMilestones.getChildCount(); i++) {
                        View child = chipGroupMilestones.getChildAt(i);
                        if (child instanceof Chip) {
                            ((Chip) child).setChecked(false);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveQuickLog(String title, String notes, String tag) {
        String id = UUID.randomUUID().toString();
        List<String> tags = new ArrayList<>();
        tags.add(tag);
        LogEntry entry = new LogEntry(id, title, notes, new ArrayList<>(),
                tags, System.currentTimeMillis(), null, null);

        db.collection("families").document(familyId).collection("logs").document(id)
                .set(entry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "⚡ Hızlı rutin kaydedildi: " + title, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Rutin kaydedilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkRelationshipStatus() {
        String bakiciId = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE).getString("username", "");
        if (bakiciId == null || bakiciId.isEmpty() || familyId == null || familyId.equals("default")) return;

        relationshipListener = db.collection("access_codes").document(familyId + "_" + bakiciId)
                .addSnapshotListener(this, (snapshot, e) -> {
                    if (e != null) return;
                    if (snapshot == null || !snapshot.exists()) {
                        Toast.makeText(this, "Bu aile ile olan ilişkiniz sonlandırılmıştır.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (relationshipListener != null) {
            relationshipListener.remove();
        }
    }
}
