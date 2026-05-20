package com.example.babylove.models;


public class User {
    private String uid;
    private String role; 
    private String displayName;
    
    
    private String city;
    private String workingHours;
    private String gender; 
    private String profileImageUrl;
    private int age;
    private int experienceYears;
    private int activeFamilyCount; 

    public User() {}

    public User(String uid, String role) {
        this.uid = uid;
        this.role = role;
    }

    public User(String uid, String role, String displayName) {
        this.uid = uid;
        this.role = role;
        this.displayName = displayName;
    }

    
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public int getActiveFamilyCount() { return activeFamilyCount; }
    public void setActiveFamilyCount(int activeFamilyCount) { this.activeFamilyCount = activeFamilyCount; }

    public boolean isVeli() { return "veli".equals(role); }
    public boolean isBakici() { return "bakici".equals(role); }
}
