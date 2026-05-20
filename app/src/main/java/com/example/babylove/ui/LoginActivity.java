package com.example.babylove.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.babylove.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnSeedDemo;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        SharedPreferences prefs = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE);
        String savedRole = prefs.getString("userRole", null);
        String savedUsername = prefs.getString("username", null);
        
        if (savedRole != null && savedUsername != null) {
            if (savedRole.equals("veli")) {
                prefs.edit().putString("familyId", savedUsername).apply();
                startActivity(new Intent(this, VeliPanelActivity.class));
                finish();
                return;
            } else if (savedRole.equals("bakici")) {
                startActivity(new Intent(this, BakiciMainActivity.class));
                finish();
                return;
            }
        } else if (prefs.getString("familyId", null) != null) {
            
            startActivity(new Intent(this, VeliPanelActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSeedDemo = findViewById(R.id.btnSeedDemo);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        btnSeedDemo.setOnClickListener(v -> handleSeedData());
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim().toLowerCase().replace(" ", "");
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen kullanıcı adı ve şifre girin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Lütfen Bekleyin...");

        db.collection("users").document(username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            
                            String dbPassword = doc.getString("password");
                            if (password.equals(dbPassword)) {
                                String role = doc.getString("role");
                                loginSuccess(username, role);
                            } else {
                                Toast.makeText(this, "Hatalı şifre!", Toast.LENGTH_SHORT).show();
                                resetLoginButton();
                            }
                        } else {
                            
                            checkLegacyFamily(username, password);
                        }
                    } else {
                        Toast.makeText(this, "Bağlantı hatası", Toast.LENGTH_SHORT).show();
                        resetLoginButton();
                    }
                });
    }

    private void checkLegacyFamily(String username, String password) {
        db.collection("families").document(username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String dbPassword = task.getResult().getString("password");
                        if (password.equals(dbPassword)) {
                            
                            migrateLegacyToUser(username, password);
                        } else {
                            Toast.makeText(this, "Hatalı şifre!", Toast.LENGTH_SHORT).show();
                            resetLoginButton();
                        }
                    } else {
                        Toast.makeText(this, "Kullanıcı bulunamadı!", Toast.LENGTH_SHORT).show();
                        resetLoginButton();
                    }
                });
    }

    private void migrateLegacyToUser(String username, String password) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);
        userData.put("role", "veli");
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(username).set(userData)
                .addOnSuccessListener(aVoid -> loginSuccess(username, "veli"))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Geçiş hatası", Toast.LENGTH_SHORT).show();
                    resetLoginButton();
                });
    }

    private void loginSuccess(String username, String role) {
        
        SharedPreferences prefs = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("userRole", role);
        
        if (role != null && role.equals("veli")) {
            editor.putString("familyId", username); 
            editor.apply();
            startActivity(new Intent(this, VeliPanelActivity.class));
        } else {
            
            editor.apply();
            startActivity(new Intent(this, BakiciMainActivity.class));
        }
        finish();
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Giriş Yap");
    }

    private void handleSeedData() {
        Log.d("LoginActivity", "Seed button clicked!");
        btnSeedDemo.setEnabled(false);
        btnLogin.setEnabled(false);
        btnSeedDemo.setText("Veriler Siliniyor...");

        
        db.collection("access_codes").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        WriteBatch deleteBatch = db.batch();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            deleteBatch.delete(doc.getReference());
                        }
                        
                        btnSeedDemo.setText("Veriler Yükleniyor...");
                        deleteBatch.commit().addOnCompleteListener(deleteTask -> {
                            
                            performSeeding();
                        });
                    } else {
                        
                        btnSeedDemo.setText("Veriler Yükleniyor...");
                        performSeeding();
                    }
                });
    }

    private void performSeeding() {
        WriteBatch batch = db.batch();

        
        for (int i = 1; i <= 10; i++) {
            String username = "bakici" + i;
            DocumentReference userRef = db.collection("users").document(username);
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("password", "123");
            userData.put("role", "bakici");
            userData.put("displayName", getBakiciName(i));
            userData.put("city", getBakiciCity(i));
            userData.put("age", getBakiciAge(i));
            userData.put("gender", getBakiciGender(i));
            userData.put("workingHours", i % 2 == 0 ? "Tam Zamanlı" : "Yarı Zamanlı");
            userData.put("experienceYears", getBakiciExperience(i));
            userData.put("profileImageUrl", getBakiciImageUrl(i));
            userData.put("createdAt", System.currentTimeMillis());
            
            batch.set(userRef, userData);
        }

        
        for (int i = 1; i <= 10; i++) {
            String username = "ev" + i;
            
            
            DocumentReference userRef = db.collection("users").document(username);
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("password", "sifre123");
            userData.put("role", "veli");
            userData.put("createdAt", System.currentTimeMillis());
            batch.set(userRef, userData);
            
            
            DocumentReference familyRef = db.collection("families").document(username);
            Map<String, Object> familyData = new HashMap<>();
            familyData.put("password", "sifre123");
            familyData.put("createdAt", System.currentTimeMillis());
            batch.set(familyRef, familyData);
            
            
            String[] clothesNames = {"Kışlık Kalın Mont", "İnce Yazlık Tişört", "Mevsimlik Hırka", "Yağmurluk", "Rüzgarlık", "Kalın Pantolon", "Yazlık Şort"};
            String[] drawables = {"ic_mont", "ic_tisort", "ic_hirka", "ic_yagmurluk", "ic_ruzgarlik", "ic_pantolon", "ic_sort"};
            String[][] clothesTags = {
                {"kışlık", "kalın", "mont", "kislik", "kalin"},
                {"yazlık", "ince", "tişört", "yazlik", "tisort"},
                {"mevsimlik", "hırka", "hirka"},
                {"mevsimlik", "yağmurluk", "su geçirmez", "yagmurluk"},
                {"mevsimlik", "rüzgarlık", "ruzgarlik"},
                {"kışlık", "kalın", "pantolon", "kislik", "kalin"},
                {"yazlık", "ince", "şort", "yazlik", "sort"}
            };
            
            for (int j = 0; j < clothesNames.length; j++) {
                String itemId = "clothes_" + i + "_" + j; 
                DocumentReference wardrobeRef = db.collection("families").document(username)
                        .collection("wardrobe").document(itemId);
                
                Map<String, Object> wardrobeData = new HashMap<>();
                wardrobeData.put("id", itemId);
                wardrobeData.put("name", clothesNames[j]);
                wardrobeData.put("imageUrl", "android.resource://com.example.babylove/drawable/" + drawables[j]);
                wardrobeData.put("tags", Arrays.asList(clothesTags[j]));
                wardrobeData.put("createdAt", System.currentTimeMillis());
                
                batch.set(wardrobeRef, wardrobeData);
            }
        }

        
        
        List<String> pool = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            pool.add("bakici" + i);
            pool.add("bakici" + i);
        }
        Collections.shuffle(pool);

        
        
        for (int i = 1; i <= 10; i++) {
            String familyId = "ev" + i;
            String bakiciId = pool.get(i - 1);
            
            String assignmentId = familyId + "_" + bakiciId;
            DocumentReference accessRef = db.collection("access_codes").document(assignmentId);
            
            Map<String, Object> assignmentData = new HashMap<>();
            assignmentData.put("familyId", familyId);
            assignmentData.put("bakiciId", bakiciId);
            assignmentData.put("status", "active");
            
            batch.set(accessRef, assignmentData);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoginActivity.this, "Demo verileri başarıyla yüklendi!", Toast.LENGTH_LONG).show();
                    resetLoginButtons();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Demo verileri yüklenirken hata oluştu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetLoginButtons();
                });
    }

    private void resetLoginButtons() {
        btnLogin.setEnabled(true);
        btnSeedDemo.setEnabled(true);
        btnSeedDemo.setText("Demo Verilerini Yükle");
    }

    private String getBakiciName(int index) {
        String[] names = {
            "Fatma Yılmaz", "Ayşe Kaya", "Zeynep Demir", "Elif Şahin", "Merve Çelik",
            "Büşra Yıldız", "Gamze Öztürk", "Hatice Aydın", "Özlem Arslan", "Selin Koç"
        };
        if (index >= 1 && index <= names.length) {
            return names[index - 1];
        }
        return "Bakıcı " + index;
    }

    private String getBakiciCity(int index) {
        String[] cities = {"Ankara", "İstanbul", "Eskişehir", "İzmir"};
        return cities[(index - 1) % cities.length];
    }

    private int getBakiciAge(int index) {
        int[] ages = {22, 28, 25, 34, 30, 27, 31, 24, 29, 35};
        if (index >= 1 && index <= ages.length) {
            return ages[index - 1];
        }
        return 20 + (index % 15);
    }

    private String getBakiciGender(int index) {
        return "Kadın";
    }

    private int getBakiciExperience(int index) {
        int[] exp = {2, 6, 4, 10, 7, 3, 5, 1, 6, 8};
        if (index >= 1 && index <= exp.length) {
            return exp[index - 1];
        }
        return 1 + (index % 8);
    }

    private String getBakiciImageUrl(int index) {
        String[] urls = {
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80",
            "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=256&q=80",
            "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=256&q=80",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=256&q=80",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80",
            "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?auto=format&fit=crop&w=256&q=80",
            "https://images.unsplash.com/photo-1548142813-c348350df52b?auto=format&fit=crop&w=256&q=80",
            "https://images.unsplash.com/photo-1506919258185-6078bba55d2a?auto=format&fit=crop&w=256&q=80",
            "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=256&q=80",
            "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=256&q=80"
        };
        if (index >= 1 && index <= urls.length) {
            return urls[index - 1];
        }
        return "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80";
    }
}

