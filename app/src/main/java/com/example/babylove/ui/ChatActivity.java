package com.example.babylove.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babylove.R;
import com.example.babylove.adapters.ChatAdapter;
import com.example.babylove.models.Message;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private MaterialButton btnSend;
    private ChatAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    private FirebaseFirestore db;
    private String familyId;
    private String userRole; 
    private String senderName; 
    private ListenerRegistration chatListener;
    private ListenerRegistration relationshipListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        familyId = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE)
                .getString("familyId", "default");

        
        userRole = getIntent().getStringExtra("role");
        if (userRole == null) {
            userRole = getSharedPreferences("BabyLovePrefs", MODE_PRIVATE).getString("userRole", "veli");
        }
        senderName = userRole.equalsIgnoreCase("veli") ? "Veli (Anne/Baba)" : "Bakıcı";

        MaterialToolbar toolbar = findViewById(R.id.toolbarChat);
        toolbar.setSubtitle(senderName + " olarak sohbet ediyorsunuz");
        toolbar.setNavigationOnClickListener(v -> finish());

        rvChat = findViewById(R.id.rvChatMessages);
        etMessage = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSendMessage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); 
        rvChat.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(userRole);
        rvChat.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        listenForMessages();

        if (userRole.equalsIgnoreCase("bakici")) {
            checkRelationshipStatus();
        }
    }

    private void listenForMessages() {
        chatListener = db.collection("families").document(familyId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(this, (snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Mesajlar yüklenemedi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        messageList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            if (message != null) {
                                if (userRole.equalsIgnoreCase("bakici") && !message.isVisibleToBakici()) {
                                    continue;
                                }
                                messageList.add(message);
                            }
                        }
                        adapter.setMessages(messageList);
                        if (!messageList.isEmpty()) {
                            rvChat.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String id = UUID.randomUUID().toString();
        Message msg = new Message(id, userRole, senderName, text, System.currentTimeMillis());

        etMessage.setText("");

        db.collection("families").document(familyId).collection("messages").document(id)
                .set(msg)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Mesaj gönderilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (chatListener != null) {
            chatListener.remove();
        }
        if (relationshipListener != null) {
            relationshipListener.remove();
        }
    }
}
