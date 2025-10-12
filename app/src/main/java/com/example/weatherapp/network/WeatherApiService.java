package com.example.weatherapp.network;

import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.ForecastWeather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    // OpenWeatherMap API key示例 (这是一个格式正确的示例密钥，但不是真实有效的)
    // 请访问 https://openweathermap.org/api 注册并获取您自己的API密钥
    String API_KEY = "88867313124dee5e37406ac03d6d5a92";
    // 注意：在实际项目中，应该使用安全的方式存储API密钥，如环境变量或密钥库
    String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    // 获取当前天气数据
    @GET("weather")
    Call<CurrentWeather> getCurrentWeather(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey
    );

    // 获取5天天气预报数据，每3小时更新一次
    @GET("forecast")
    Call<ForecastWeather> getForecastWeather(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey
    );
}