package com.example.weatherapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {
    private static final String PREF_NAME = "weather_app_prefs";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LAST_UPDATE_TIME = "last_update_time";
    private static final String KEY_CITY_NAME = "city_name";
    private static final String KEY_HAS_CACHED_LOCATION = "has_cached_location";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_FIRST_LAUNCH = "first_launch";

    private final SharedPreferences sharedPreferences;

    public PreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 缓存经纬度信息
    public void saveLocation(double latitude, double longitude, String cityName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_HAS_CACHED_LOCATION, true);
        editor.putFloat(KEY_LATITUDE, (float) latitude);
        editor.putFloat(KEY_LONGITUDE, (float) longitude);
        editor.putString(KEY_CITY_NAME, cityName);
        editor.apply();
    }

    // 保存最后更新时间
    public void saveLastUpdateTime(long timestamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_UPDATE_TIME, timestamp);
        editor.apply();
    }

    // 获取缓存的纬度
    public double getLatitude() {
        return sharedPreferences.getFloat(KEY_LATITUDE, 0f);
    }

    // 获取缓存的经度
    public double getLongitude() {
        return sharedPreferences.getFloat(KEY_LONGITUDE, 0f);
    }

    // 获取缓存的城市名称
    public String getCityName() {
        return sharedPreferences.getString(KEY_CITY_NAME, "");
    }
    
    // 保存城市名称
    public void saveCityName(String cityName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CITY_NAME, cityName);
        editor.apply();
    }

    // 获取最后更新时间
    public long getLastUpdateTime() {
        return sharedPreferences.getLong(KEY_LAST_UPDATE_TIME, 0);
    }

    // 检查是否有缓存的位置信息
    public boolean hasCachedLocation() {
        return sharedPreferences.getBoolean(KEY_HAS_CACHED_LOCATION, false);
    }

    // 清除缓存的位置信息
    public void clearCachedLocation() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_HAS_CACHED_LOCATION);
        editor.remove(KEY_LATITUDE);
        editor.remove(KEY_LONGITUDE);
        editor.remove(KEY_CITY_NAME);
        editor.apply();
    }

    // 检查是否需要更新天气数据（超过5分钟）
    public boolean isWeatherDataExpired() {
        long lastUpdateTime = getLastUpdateTime();
        if (lastUpdateTime == 0) {
            return true; // 从未更新过，需要更新
        }
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastUpdateTime;
        return timeDifference > 5 * 60 * 1000; // 5分钟
    }
    
    // 保存API key
    public void saveApiKey(String apiKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_API_KEY, apiKey);
        editor.apply();
    }
    
    // 获取缓存的API key
    public String getApiKey() {
        return sharedPreferences.getString(KEY_API_KEY, "");
    }
    
    // 检查是否有缓存的API key
    public boolean hasApiKey() {
        String apiKey = getApiKey();
        return apiKey != null && !apiKey.isEmpty();
    }

    // 检查是否是首次启动应用
    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    // 设置首次启动标志为false（表示已经启动过）
    public void setAppLaunched() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.apply();
    }
}