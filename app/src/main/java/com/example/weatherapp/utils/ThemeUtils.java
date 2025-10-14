package com.example.weatherapp.utils;

import android.app.Activity;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Calendar;

/**
 * 主题工具类，用于处理应用的日/夜间模式切换和相关判断
 */
public class ThemeUtils {
    private static final String TAG = "ThemeUtils";
    private static final int DAY_START_HOUR = 6; // 白天开始时间：早上6点
    private static final int DAY_END_HOUR = 19; // 白天结束时间：晚上7点

    /**
     * 根据当前时间设置应用的日/夜间模式
     * 设置在早上6点到晚上7点之间为白天模式，其他时间为夜间模式
     * @param activity AppCompatActivity实例
     */
    public static void setThemeBasedOnTime(AppCompatActivity activity) {
        if (isDayTime()) {
            // 白天模式
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Log.d(TAG, "设置为白天模式");
        } else {
            // 夜间模式
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Log.d(TAG, "设置为夜间模式");
        }
    }

    /**
     * 判断当前是否为白天时间（早上6点到晚上7点）
     * @return 如果是白天返回true，否则返回false
     */
    public static boolean isDayTime() {
        int currentHour = getCurrentHour();
        return currentHour >= DAY_START_HOUR && currentHour < DAY_END_HOUR;
    }

    /**
     * 获取当前时间的小时数（24小时制）
     * @return 当前小时数
     */
    public static int getCurrentHour() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
}