package com.example.babylove.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.babylove.R;


public class RoleActivity extends AppCompatActivity {

    private String familyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        familyId = getIntent().getStringExtra("familyId");
        if (familyId == null) {
            familyId = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE)
                    .getString("familyId", "");
        }

        
        TextView tvWelcome = findViewById(R.id.tvWelcomeFamily);
        tvWelcome.setText("Hoş Geldiniz, " + familyId + "! 👶");

        
        Button btnVeli = findViewById(R.id.btnVeli);
        btnVeli.setOnClickListener(v -> {
            startActivity(new Intent(this, VeliPanelActivity.class));
        });

        
        Button btnBakici = findViewById(R.id.btnBakici);
        btnBakici.setOnClickListener(v -> {
            startActivity(new Intent(this, BakiciPanelActivity.class));
        });

        
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarRole);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        
        logout();
        return true;
    }

    @Override
    public void onBackPressed() {
        
        logout();
    }

    
    private void logout() {
        SharedPreferences prefs = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE);
        prefs.edit().remove("familyId").apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
