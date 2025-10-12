package com.example.weatherapp.network;

import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.ForecastWeather;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherRepository {
    private static WeatherRepository instance;
    private WeatherApiService weatherApiService;

    private WeatherRepository() {
        // 创建Retrofit实例
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WeatherApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 创建API服务
        weatherApiService = retrofit.create(WeatherApiService.class);
    }

    public static synchronized WeatherRepository getInstance() {
        if (instance == null) {
            instance = new WeatherRepository();
        }
        return instance;
    }

    // 获取当前天气数据
    public Call<CurrentWeather> getCurrentWeather(double latitude, double longitude) {
        return weatherApiService.getCurrentWeather(latitude, longitude, WeatherApiService.API_KEY);
    }

    // 获取天气预报数据
    public Call<ForecastWeather> getForecastWeather(double latitude, double longitude) {
        return weatherApiService.getForecastWeather(latitude, longitude, WeatherApiService.API_KEY);
    }
}