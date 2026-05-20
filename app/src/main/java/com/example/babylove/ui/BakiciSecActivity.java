package com.example.babylove.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babylove.R;
import com.example.babylove.adapters.BakiciAdapter;
import com.example.babylove.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentReference;
import java.util.UUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BakiciSecActivity extends AppCompatActivity {

    private RecyclerView rvBakiciList;
    private BakiciAdapter adapter;
    private FirebaseFirestore db;
    private String familyId;
    
    private EditText etFilterCity;
    private Spinner spinnerFilterGender;

    private List<User> allBakicilar = new ArrayList<>();
    private String currentBakiciId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bakici_sec);

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE);
        familyId = prefs.getString("familyId", null);

        MaterialToolbar toolbar = findViewById(R.id.toolbarBakiciSec);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFilterCity = findViewById(R.id.etFilterCity);
        spinnerFilterGender = findViewById(R.id.spinnerFilterGender);
        
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Tümü", "Kadın", "Erkek"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterGender.setAdapter(genderAdapter);

        rvBakiciList = findViewById(R.id.rvBakiciList);
        rvBakiciList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BakiciAdapter();
        rvBakiciList.setAdapter(adapter);

        adapter.setOnBakiciSelectedListener(this::handleBakiciSelection);

        etFilterCity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        spinnerFilterGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { applyFilters(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        findViewById(R.id.btnFilter).setOnClickListener(v -> applyFilters());

        loadData();
    }

    private void loadData() {
        
        db.collection("access_codes").whereEqualTo("status", "active").get()
                .addOnSuccessListener(task -> {
                    Map<String, Integer> bakiciCounts = new HashMap<>();
                    currentBakiciId = null;

                    for (DocumentSnapshot doc : task.getDocuments()) {
                        String bId = doc.getString("bakiciId");
                        String fId = doc.getString("familyId");

                        if (fId != null && fId.equals(familyId)) {
                            currentBakiciId = bId;
                        }

                        if (bId != null) {
                            bakiciCounts.put(bId, bakiciCounts.getOrDefault(bId, 0) + 1);
                        }
                    }

                    
                    db.collection("users").whereEqualTo("role", "bakici").get()
                            .addOnSuccessListener(userTask -> {
                                allBakicilar.clear();
                                User currentBakiciObj = null;

                                for (DocumentSnapshot doc : userTask.getDocuments()) {
                                    User user = doc.toObject(User.class);
                                    if (user != null) {
                                        user.setUid(doc.getId());
                                        user.setActiveFamilyCount(bakiciCounts.getOrDefault(user.getUid(), 0));
                                        
                                        if (currentBakiciId != null && user.getUid().equals(currentBakiciId)) {
                                            currentBakiciObj = user;
                                        } else {
                                            allBakicilar.add(user);
                                        }
                                    }
                                }

                                
                                if (currentBakiciObj != null) {
                                    allBakicilar.add(0, currentBakiciObj);
                                }

                                applyFilters();
                            });
                });
    }

    private void applyFilters() {
        String cityFilter = etFilterCity.getText().toString().trim().toLowerCase();
        int genderIndex = spinnerFilterGender.getSelectedItemPosition();
        String genderFilter = genderIndex == 1 ? "Kadın" : (genderIndex == 2 ? "Erkek" : "");

        List<User> filteredList = new ArrayList<>();

        for (User u : allBakicilar) {
            boolean matchCity = cityFilter.isEmpty() || (u.getCity() != null && u.getCity().toLowerCase().contains(cityFilter));
            boolean matchGender = genderFilter.isEmpty() || (u.getGender() != null && u.getGender().equals(genderFilter));

            if (matchCity && matchGender) {
                filteredList.add(u);
            }
        }

        adapter.setBakiciList(filteredList);
    }

    private void handleBakiciSelection(User bakici) {
        if (currentBakiciId != null && bakici.getUid().equals(currentBakiciId)) {
            
            new AlertDialog.Builder(this)
                    .setTitle("İlişkiyi Kes")
                    .setMessage(bakici.getUid() + " adlı bakıcı ile olan çalışmanızı sonlandırmak istiyor musunuz?")
                    .setPositiveButton("Evet", (dialog, which) -> severTies(bakici))
                    .setNegativeButton("Hayır", null)
                    .show();
        } else if (currentBakiciId != null) {
            
            Toast.makeText(this, "Önce mevcut bakıcınızla iletişimi kesmelisiniz.", Toast.LENGTH_LONG).show();
        } else {
            
            new AlertDialog.Builder(this)
                    .setTitle("Bakıcı Seçimi")
                    .setMessage(bakici.getUid() + " adlı kullanıcıyı bakıcı olarak atamak istiyor musunuz?")
                    .setPositiveButton("Evet", (dialog, which) -> assignBakici(bakici))
                    .setNegativeButton("Hayır", null)
                    .show();
        }
    }

    private void severTies(User bakici) {
        db.collection("families").document(familyId).collection("messages").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        DocumentReference msgRef = doc.getReference();
                        batch.update(msgRef, "visibleToBakici", false);
                    }

                    
                    String systemMsgId = UUID.randomUUID().toString();
                    DocumentReference newMsgRef = db.collection("families").document(familyId)
                            .collection("messages").document(systemMsgId);

                    Map<String, Object> systemMsg = new HashMap<>();
                    systemMsg.put("id", systemMsgId);
                    systemMsg.put("senderId", "system");
                    systemMsg.put("senderName", "Sistem");
                    systemMsg.put("text", "Bakıcı değişikliği yapıldı. Yeni bakıcı eski mesajları göremeyecektir.");
                    systemMsg.put("timestamp", System.currentTimeMillis());
                    systemMsg.put("visibleToBakici", false);
                    batch.set(newMsgRef, systemMsg);

                    
                    String assignmentId = familyId + "_" + bakici.getUid();
                    DocumentReference assignmentRef = db.collection("access_codes").document(assignmentId);
                    batch.delete(assignmentRef);

                    
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Bakıcı ile ilişik kesildi ve sohbet geçmişi sıfırlandı.", Toast.LENGTH_LONG).show();
                                loadData(); 
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Mesajlar okunurken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void assignBakici(User bakici) {
        if (bakici.getActiveFamilyCount() >= 2) {
            Toast.makeText(this, "Bu bakıcı maksimum aile sayısına ulaşmış.", Toast.LENGTH_SHORT).show();
            return;
        }

        String assignmentId = familyId + "_" + bakici.getUid();
        Map<String, Object> assignmentData = new HashMap<>();
        assignmentData.put("familyId", familyId);
        assignmentData.put("bakiciId", bakici.getUid());
        assignmentData.put("status", "active");

        db.collection("access_codes").document(assignmentId).set(assignmentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Atama Başarılı!", Toast.LENGTH_SHORT).show();
                    loadData(); 
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Atama başarısız oldu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
