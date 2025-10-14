package com.example.weatherapp.network;

import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.ForecastWeather;
import com.example.weatherapp.model.ReverseGeocodingResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    // 注意：API密钥应从安全的地方获取，如用户配置
    String BASE_URL = "https://api.openweathermap.org/";

    // 获取当前天气数据
    @GET("data/2.5/weather")
    Call<CurrentWeather> getCurrentWeather(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey
    );

    // 获取5天天气预报数据，每3小时更新一次
    @GET("data/2.5/forecast")
    Call<ForecastWeather> getForecastWeather(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey
    );

    // 反向地理编码（通过经纬度获取位置信息）
    @GET("data/2.5/weather")
    Call<CurrentWeather> getLocationInfo(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey
    );

    // 正确的反向地理编码API（使用OpenWeatherMap的地理编码API）
    @GET("geo/1.0/reverse")
    Call<ReverseGeocodingResponse[]> getReverseGeocodingInfo(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}