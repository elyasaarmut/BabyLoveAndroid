package com.example.babylove.utils;

import com.example.babylove.models.WardrobeItem;
import com.example.babylove.network.WeatherResponse;

import java.util.ArrayList;
import java.util.List;


public class WeatherRecommender {

    
    public static class RecommendationResult {
        public final List<WardrobeItem> wearThese;     
        public final List<WardrobeItem> dontWearThese;  
        public final String summary;                     

        public RecommendationResult(List<WardrobeItem> wearThese,
                                     List<WardrobeItem> dontWearThese,
                                     String summary) {
            this.wearThese = wearThese;
            this.dontWearThese = dontWearThese;
            this.summary = summary;
        }
    }

    
    public static RecommendationResult recommend(WeatherResponse weather,
                                                  List<WardrobeItem> allItems) {
        double temp = weather.main.temp;
        double wind = weather.wind.speed;
        boolean isRainy = weather.isRainy();
        int humidity = weather.main.humidity;

        
        List<String> wearTags = new ArrayList<>();
        
        List<String> dontWearTags = new ArrayList<>();

        StringBuilder summary = new StringBuilder();

        
        if (temp < 10) {
            
            wearTags.add("kışlık");
            wearTags.add("kalın");
            wearTags.add("mont");
            dontWearTags.add("yazlık");
            dontWearTags.add("ince");
            dontWearTags.add("şort");
            summary.append("Hava çok soğuk (").append(String.format("%.0f", temp))
                   .append("°C), kalın kıyafetler giydirilmeli. ");
        } else if (temp < 18) {
            
            wearTags.add("kışlık");
            wearTags.add("kalın");
            dontWearTags.add("yazlık");
            dontWearTags.add("ince");
            summary.append("Hava serin (").append(String.format("%.0f", temp))
                   .append("°C), katmanlı giydirilmeli. ");
        } else if (temp < 25) {
            
            wearTags.add("yazlık");
            wearTags.add("ince");
            wearTags.add("mevsimlik");
            dontWearTags.add("kışlık");
            dontWearTags.add("kalın");
            dontWearTags.add("mont");
            summary.append("Hava ılık (").append(String.format("%.0f", temp))
                   .append("°C), hafif kıyafetler uygundur. ");
        } else {
            
            wearTags.add("yazlık");
            wearTags.add("ince");
            dontWearTags.add("kışlık");
            dontWearTags.add("kalın");
            dontWearTags.add("mont");
            dontWearTags.add("rüzgarlık");
            summary.append("Hava sıcak (").append(String.format("%.0f", temp))
                   .append("°C), en ince kıyafetler giydirilmeli. ");
        }

        
        if (wind > 10) {
            wearTags.add("rüzgarlık");
            summary.append("Rüzgar şiddetli (").append(String.format("%.0f", wind))
                   .append(" m/s), rüzgarlık önerilir. ");
        } else if (wind > 5) {
            summary.append("Hafif rüzgar var. ");
        }

        
        if (isRainy) {
            wearTags.add("yağmurluk");
            wearTags.add("su geçirmez");
            summary.append("Yağmur bekleniyor, yağmurluk şart! ");
        }

        
        if (humidity > 80 && temp > 25) {
            summary.append("Nem yüksek (%").append(humidity)
                   .append("), nefes alan kumaşlar tercih edilmeli. ");
        }

        
        List<WardrobeItem> wearThese = new ArrayList<>();
        List<WardrobeItem> dontWearThese = new ArrayList<>();

        for (WardrobeItem item : allItems) {
            if (item.getTags() == null) continue;

            boolean shouldWear = false;
            boolean shouldNotWear = false;

            for (String tag : item.getTags()) {
                if (tag == null) continue;
                String lowerTag = tag.toLowerCase().trim();
                if (wearTags.contains(lowerTag)) {
                    shouldWear = true;
                }
                if (dontWearTags.contains(lowerTag)) {
                    shouldNotWear = true;
                }
            }

            if (shouldWear && !shouldNotWear) {
                wearThese.add(item);
            } else if (shouldNotWear) {
                dontWearThese.add(item);
            }
        }

        return new RecommendationResult(wearThese, dontWearThese, summary.toString().trim());
    }
}