package com.example.babylove.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;


public class WeatherResponse {

    @SerializedName("main")
    public Main main;

    @SerializedName("wind")
    public Wind wind;

    @SerializedName("weather")
    public List<Weather> weather;

    @SerializedName("name")
    public String cityName;

    
    public static class Main {
        @SerializedName("temp")
        public double temp; 

        @SerializedName("feels_like")
        public double feelsLike;

        @SerializedName("humidity")
        public int humidity; 
    }

    
    public static class Wind {
        @SerializedName("speed")
        public double speed; 
    }

    
    public static class Weather {
        @SerializedName("id")
        public int id;

        @SerializedName("main")
        public String main; 

        @SerializedName("description")
        public String description; 

        @SerializedName("icon")
        public String icon; 
    }

    

    
    public boolean isRainy() {
        if (weather == null || weather.isEmpty()) return false;
        String mainWeather = weather.get(0).main.toLowerCase();
        return mainWeather.contains("rain") || mainWeather.contains("drizzle") ||
               mainWeather.contains("thunderstorm");
    }

    
    public boolean isSnowy() {
        if (weather == null || weather.isEmpty()) return false;
        return weather.get(0).main.toLowerCase().contains("snow");
    }

    
    public String getIconUrl() {
        if (weather == null || weather.isEmpty()) return "";
        return "https://openweathermap.org/img/wn/" + weather.get(0).icon + "@2x.png";
    }

    
    public String getDescription() {
        if (weather == null || weather.isEmpty()) return "";
        return weather.get(0).description;
    }
}
