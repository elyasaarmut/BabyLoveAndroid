package com.example.babylove.models;

import java.util.ArrayList;
import java.util.List;


public class WardrobeItem {
    private String id;
    private String name;          
    private String imageUrl;      
    private List<String> tags;    
    private long createdAt;       

    public WardrobeItem() {
        this.tags = new ArrayList<>();
    }

    public WardrobeItem(String id, String name, String imageUrl, List<String> tags) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    
    public boolean hasAnyTag(List<String> searchTags) {
        if (tags == null || searchTags == null) return false;
        for (String tag : tags) {
            if (searchTags.contains(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    
    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            sb.append(tags.get(i));
            if (i < tags.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
