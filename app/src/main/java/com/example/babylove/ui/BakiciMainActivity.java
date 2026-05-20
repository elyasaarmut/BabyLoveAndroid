package com.example.babylove.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babylove.R;
import com.example.babylove.adapters.FamilyAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BakiciMainActivity extends AppCompatActivity {

    private RecyclerView rvFamilyList;
    private FamilyAdapter adapter;
    private FirebaseFirestore db;
    private String bakiciId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bakici_main);

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE);
        bakiciId = prefs.getString("username", null);

        TextView tvWelcome = findViewById(R.id.tvWelcomeBakici);
        tvWelcome.setText("Hoş Geldiniz, " + bakiciId + " 👩‍⚕️");

        rvFamilyList = findViewById(R.id.rvFamilyList);
        rvFamilyList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FamilyAdapter();
        rvFamilyList.setAdapter(adapter);

        adapter.setOnFamilySelectedListener(this::goToFamilyPanel);

        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, BakiciProfileActivity.class));
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        loadAssignedFamilies();
    }

    private void loadAssignedFamilies() {
        db.collection("access_codes")
                .whereEqualTo("bakiciId", bakiciId)
                .whereEqualTo("status", "active")
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Aileler yüklenemedi", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        List<Map<String, Object>> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            list.add(doc.getData());
                        }
                        adapter.setFamilyList(list);
                    }
                });
    }

    private void goToFamilyPanel(Map<String, Object> assignment) {
        String familyId = (String) assignment.get("familyId");

        
        SharedPreferences prefs = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE);
        prefs.edit().putString("familyId", familyId).apply();

        Intent intent = new Intent(this, BakiciPanelActivity.class);
        startActivity(intent);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
