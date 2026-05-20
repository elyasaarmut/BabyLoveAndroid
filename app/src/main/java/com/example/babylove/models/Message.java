package com.example.babylove.models;

import com.google.firebase.firestore.PropertyName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Message {
    private String id;
    private String senderId;     
    private String senderName;   
    private String text;         
    private long timestamp;      

    @PropertyName("visibleToBakici")
    private boolean visibleToBakici = true; 

    public Message() {}

    public Message(String id, String senderId, String senderName, String text, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.visibleToBakici = true;
    }

    public Message(String id, String senderId, String senderName, String text, long timestamp, boolean visibleToBakici) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.visibleToBakici = visibleToBakici;
    }

    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @PropertyName("visibleToBakici")
    public boolean isVisibleToBakici() { return visibleToBakici; }

    @PropertyName("visibleToBakici")
    public void setVisibleToBakici(boolean visibleToBakici) { this.visibleToBakici = visibleToBakici; }

    
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", new Locale("tr"));
        return sdf.format(new Date(timestamp));
    }
}
