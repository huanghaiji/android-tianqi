package com.example.weatherapp.network;

import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.ForecastWeather;
import com.example.weatherapp.model.ReverseGeocodingResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherRepository {
    private static WeatherRepository instance;
    private WeatherApiService weatherApiService;
    private String apiKey;

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

    // 设置API key
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    // 获取API key（如果未设置则返回默认API Key）
    public String getApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            // 返回默认API Key作为备选方案
            // 注意：这仅用于演示，实际应用中应鼓励用户设置自己的API Key
            return "88867313124dee5e37406ac03d6d5a92";
        }
        return apiKey;
    }

    // 获取当前天气数据
    public Call<CurrentWeather> getCurrentWeather(double latitude, double longitude) {
        return weatherApiService.getCurrentWeather(latitude, longitude, getApiKey());
    }

    // 获取天气预报数据
    public Call<ForecastWeather> getForecastWeather(double latitude, double longitude) {
        return weatherApiService.getForecastWeather(latitude, longitude, getApiKey());
    }

    // 获取位置信息（通过经纬度获取城市名称等）
    public Call<CurrentWeather> getLocationInfo(double latitude, double longitude) {
        return weatherApiService.getLocationInfo(latitude, longitude, getApiKey());
    }

    // 获取反向地理编码信息
    public Call<ReverseGeocodingResponse[]> getReverseGeocodingInfo(double latitude, double longitude) {
        return weatherApiService.getReverseGeocodingInfo(latitude, longitude, 1, getApiKey());
    }
}