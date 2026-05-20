package com.example.babylove.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class LogEntry {
    private String id;
    private String title;                 
    private String notes;                 
    private List<String> imageUrls;       
    private List<String> milestoneTags;   
    private long timestamp;               
    private Double height;                
    private Double weight;                

    public LogEntry() {
        this.imageUrls = new ArrayList<>();
        this.milestoneTags = new ArrayList<>();
    }

    public LogEntry(String id, String title, String notes,
                    List<String> imageUrls, List<String> milestoneTags, long timestamp) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.milestoneTags = milestoneTags != null ? milestoneTags : new ArrayList<>();
        this.timestamp = timestamp;
        this.height = null;
        this.weight = null;
    }

    public LogEntry(String id, String title, String notes,
                    List<String> imageUrls, List<String> milestoneTags, long timestamp, Double height, Double weight) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.milestoneTags = milestoneTags != null ? milestoneTags : new ArrayList<>();
        this.timestamp = timestamp;
        this.height = height;
        this.weight = weight;
    }

    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getMilestoneTags() { return milestoneTags; }
    public void setMilestoneTags(List<String> milestoneTags) { this.milestoneTags = milestoneTags; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("tr"));
        return sdf.format(new Date(timestamp));
    }

    
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", new Locale("tr"));
        return sdf.format(new Date(timestamp));
    }

    
    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    
    public boolean hasMilestones() {
        return milestoneTags != null && !milestoneTags.isEmpty();
    }
}
