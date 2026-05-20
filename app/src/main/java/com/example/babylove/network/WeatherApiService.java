package com.example.babylove.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface WeatherApiService {

    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
        @Query("q") String cityName,
        @Query("appid") String apiKey,
        @Query("units") String units,     
        @Query("lang") String language    
    );

    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeatherByCoords(
        @Query("lat") double lat,
        @Query("lon") double lon,
        @Query("appid") String apiKey,
        @Query("units") String units,
        @Query("lang") String language
    );
}
