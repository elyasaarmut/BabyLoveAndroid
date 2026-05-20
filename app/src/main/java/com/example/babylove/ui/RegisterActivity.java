package com.example.babylove.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.babylove.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgRole;
    private Button btnRegister;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.etRegUsername);
        etPassword = findViewById(R.id.etRegPassword);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> handleRegister());

        tvGoToLogin.setOnClickListener(v -> {
            finish(); 
        });
    }

    private void handleRegister() {
        String username = etUsername.getText().toString().trim().toLowerCase().replace(" ", "");
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = rgRole.getCheckedRadioButtonId();
        String role = (selectedId == R.id.rbBakici) ? "bakici" : "veli";

        btnRegister.setEnabled(false);
        btnRegister.setText("Kayıt Olunuyor...");

        
        db.collection("users").document(username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            Toast.makeText(this, "Bu kullanıcı adı zaten alınmış", Toast.LENGTH_SHORT).show();
                            resetButton();
                        } else {
                            createUser(username, password, role);
                        }
                    } else {
                        Toast.makeText(this, "Bağlantı hatası", Toast.LENGTH_SHORT).show();
                        resetButton();
                    }
                });
    }

    private void createUser(String username, String password, String role) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);
        userData.put("role", role);
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(username).set(userData)
                .addOnSuccessListener(aVoid -> {
                    if (role.equals("veli")) {
                        
                        Map<String, Object> familyData = new HashMap<>();
                        familyData.put("password", password); 
                        familyData.put("createdAt", System.currentTimeMillis());
                        
                        db.collection("families").document(username).set(familyData)
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show();
                                    finish(); 
                                });
                    } else {
                        Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show();
                        finish(); 
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Kayıt başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetButton();
                });
    }

    private void resetButton() {
        btnRegister.setEnabled(true);
        btnRegister.setText("Kayıt Ol");
    }
}
