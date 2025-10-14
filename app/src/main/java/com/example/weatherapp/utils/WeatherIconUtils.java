package com.example.weatherapp.utils;

import android.util.Log;
import com.example.weatherapp.R;

/**
 * 天气图标工具类，用于处理天气图标的加载和显示
 */
public class WeatherIconUtils {
    private static final String TAG = "WeatherIconUtils";

    /**
     * 根据OpenWeatherMap的图标代码获取对应的本地资源ID
     * @param iconCode OpenWeatherMap的图标代码
     * @return 对应的本地资源ID
     */
    public static int getLocalWeatherIcon(String iconCode) {
        Log.d(TAG, "Using local weather icon for code: " + iconCode);

        // 根据OpenWeatherMap的图标代码映射到本地资源
        int resourceId;

        if (iconCode.contains("01")) {
            // 晴天
            resourceId = R.drawable.ic_sunny;
        } else if (iconCode.contains("02")) {
            // 少云
            resourceId = R.drawable.ic_partly_cloudy;
        } else if (iconCode.contains("03") || iconCode.contains("04")) {
            // 多云系列
            resourceId = R.drawable.ic_cloudy;
        } else if (iconCode.contains("09") || iconCode.contains("10")) {
            // 雨
            resourceId = R.drawable.ic_rainy;
        } else if (iconCode.contains("11")) {
            // 雷暴
            resourceId = R.drawable.ic_stormy;
        } else if (iconCode.contains("13")) {
            // 雪
            resourceId = R.drawable.ic_snowy;
        } else if (iconCode.contains("50")) {
            // 雾
            resourceId = R.drawable.ic_foggy;
        } else {
            // 未知天气
            resourceId = R.drawable.ic_unknown;
        }

        return resourceId;
    }
}