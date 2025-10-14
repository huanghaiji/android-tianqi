package com.example.weatherapp.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类，用于处理应用中的时间格式化和解析
 */
public class TimeUtils {
    private static final String TAG = "TimeUtils";
    
    // API返回的时间格式
    private static final String API_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // 显示时间格式（小时:分钟）
    private static final String TIME_FORMAT = "HH:mm";
    // 显示日期时间格式（月-日 小时:分钟）
    private static final String DATE_TIME_FORMAT = "MM-dd HH:mm";
    // 显示日期格式（月-日 星期几）
    private static final String DATE_WITH_WEEK_FORMAT = "MM-dd EEEE";
    // 仅日期格式（年-月-日）
    private static final String DATE_ONLY_FORMAT = "yyyy-MM-dd";

    /**
     * 格式化当前时间为小时:分钟格式
     * @return 格式化后的时间字符串
     */
    public static String formatCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return timeFormat.format(new Date());
    }

    /**
     * 格式化指定时间戳为小时:分钟格式
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的时间字符串
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }

    /**
     * 将API返回的日期时间格式转换为更友好的格式（月-日 小时:分钟）
     * @param dateTimeStr API返回的日期时间字符串
     * @return 格式化后的日期时间字符串
     */
    public static String formatApiDateTime(String dateTimeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
            Date date = inputFormat.parse(dateTimeStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date time: " + dateTimeStr, e);
            return dateTimeStr;
        }
    }

    /**
     * 将API返回的日期时间格式转换为日期加星期格式（月-日 星期几）
     * @param dateTimeStr API返回的日期时间字符串
     * @return 格式化后的日期字符串
     */
    public static String formatDateWithWeek(String dateTimeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
            Date date = inputFormat.parse(dateTimeStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat(DATE_WITH_WEEK_FORMAT, Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date time: " + dateTimeStr, e);
            return dateTimeStr;
        }
    }

    /**
     * 从API返回的日期时间字符串中提取日期部分（年-月-日）
     * @param dateTimeStr API返回的日期时间字符串
     * @return 提取的日期字符串
     */
    public static String extractDate(String dateTimeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
            Date date = inputFormat.parse(dateTimeStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat(DATE_ONLY_FORMAT, Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date time: " + dateTimeStr, e);
            return dateTimeStr;
        }
    }

    /**
     * 解析API返回的日期时间字符串为Date对象
     * @param dateTimeStr API返回的日期时间字符串
     * @return 解析后的Date对象
     * @throws ParseException 解析失败时抛出异常
     */
    public static Date parseApiDateTime(String dateTimeStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault());
        return dateFormat.parse(dateTimeStr);
    }

    /**
     * 检查指定的预报时间是否已经过去
     * @param forecastDateTimeStr 预报的日期时间字符串
     * @return 如果时间已过去返回true，否则返回false
     */
    public static boolean isForecastTimePast(String forecastDateTimeStr) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            Date forecastDate = parseApiDateTime(forecastDateTimeStr);
            long forecastTimeMillis = forecastDate.getTime();
            return forecastTimeMillis < currentTimeMillis;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing forecast date: " + forecastDateTimeStr, e);
            return false; // 解析失败时默认认为时间未过去
        }
    }
}