package com.example.weatherapp;

import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;

// import com.squareup.picasso.Picasso;
import com.example.weatherapp.utils.ImageLoader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.model.ForecastWeather;
import com.example.weatherapp.view.TemperatureProgressBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private static final String TAG = "ForecastAdapter";
    private List<ForecastWeather.ForecastItem> forecastItems;
    private double overallMinTemp = Double.MAX_VALUE;
    private double overallMaxTemp = Double.MIN_VALUE;

    public ForecastAdapter(List<ForecastWeather.ForecastItem> forecastItems) {
        this.forecastItems = forecastItems;
        calculateOverallTemperatureRange();
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.forecast_item, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastWeather.ForecastItem forecastItem = forecastItems.get(position);
        // 获取上一条数据的温度（如果存在）
        double previousTemp = Double.NaN;
        if (position > 0 && forecastItems.get(position - 1).getMain() != null) {
            previousTemp = forecastItems.get(position - 1).getMain().getTemp() - 273.15;
        }
        holder.bind(forecastItem, previousTemp, position);
    }

    @Override
    public int getItemCount() {
        return forecastItems != null ? forecastItems.size() : 0;
    }

    // 更新数据
    public void updateData(List<ForecastWeather.ForecastItem> newForecastItems) {
        this.forecastItems = newForecastItems;
        calculateOverallTemperatureRange();
        notifyDataSetChanged();
    }

    // 计算所有数据中的温度范围
    private void calculateOverallTemperatureRange() {
        if (forecastItems == null || forecastItems.isEmpty()) {
            return;
        }

        overallMinTemp = Double.MAX_VALUE;
        overallMaxTemp = Double.MIN_VALUE;

        for (ForecastWeather.ForecastItem item : forecastItems) {
            if (item.getMain() != null) {
                double temp = item.getMain().getTemp() - 273.15;
                overallMinTemp = Math.min(overallMinTemp, temp);
                overallMaxTemp = Math.max(overallMaxTemp, temp);
            }
        }

        // 确保温度范围有效
        if (overallMinTemp == Double.MAX_VALUE || overallMaxTemp == Double.MIN_VALUE) {
            overallMinTemp = -10; // 默认最小值
            overallMaxTemp = 40;  // 默认最大值
        } else if (overallMinTemp == overallMaxTemp) {
            // 如果所有温度都相同，设置一个小的范围
            overallMinTemp -= 1;
            overallMaxTemp += 1;
        }

        Log.d(TAG, "Overall temperature range: " + overallMinTemp + "°C to " + overallMaxTemp + "°C");
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {

        private TextView forecastTimeTextView;
        private ImageView forecastIconImageView;
        private TextView forecastDescriptionTextView;
        private TextView forecastTemperatureTextView;
        private TemperatureProgressBar temperatureProgressBar;
        private TextView minTempLabel;
        private TextView maxTempLabel;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            forecastTimeTextView = itemView.findViewById(R.id.forecast_time);
            forecastIconImageView = itemView.findViewById(R.id.forecast_icon);
            forecastDescriptionTextView = itemView.findViewById(R.id.forecast_description);
            forecastTemperatureTextView = itemView.findViewById(R.id.forecast_temperature);
            temperatureProgressBar = itemView.findViewById(R.id.temperature_progress);
            minTempLabel = itemView.findViewById(R.id.min_temp_label);
            maxTempLabel = itemView.findViewById(R.id.max_temp_label);
        }

        public void bind(ForecastWeather.ForecastItem forecastItem, double previousTemp, int position) {
            // 设置时间（格式化日期时间）
            String formattedTime = formatDateTime(forecastItem.getDt_txt());
            forecastTimeTextView.setText(formattedTime);

            // 设置天气描述
            if (forecastItem.getWeather() != null && !forecastItem.getWeather().isEmpty()) {
                String description = forecastItem.getWeather().get(0).getDescription();
                forecastDescriptionTextView.setText(description);

                // 设置天气图标 - 使用OpenWeatherMap的官方图标
                String iconCode = forecastItem.getWeather().get(0).getIcon();
                Log.d(TAG, "Loading forecast icon: " + iconCode);
                String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@3x.png";

                // 正常加载，会使用缓存
                // 获取Context（从forecastIconImageView获取）
                android.content.Context context = forecastIconImageView.getContext();
                ImageLoader.getInstance(context).loadImage(iconUrl, forecastIconImageView, new ImageLoader.ImageLoadCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Forecast icon loaded successfully: " + iconCode);
                        // 确保图标颜色在白天模式下正确显示
                        int currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
                        if (currentHour >= 6 && currentHour < 19) {
                            // 白天模式 - 确保图标不透明且颜色正确
                            forecastIconImageView.setColorFilter(null); // 清除任何颜色滤镜
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to load forecast icon: " + e.getMessage());
                        // 如果网络加载失败，尝试使用本地图标
                        useLocalForecastIcon(iconCode);
                    }
                });
            }

            // 设置温度范围（开尔文转摄氏度）和进度条
            if (forecastItem.getMain() != null) {
                double tempMin = forecastItem.getMain().getTemp_min() - 273.15;
                double tempMax = forecastItem.getMain().getTemp_max() - 273.15;
                double tempCurr= forecastItem.getMain().getTemp() - 273.15;

                // 添加日志记录温度数据，便于调试
                Log.d(TAG, "Forecast temperature for " + forecastItem.getDt_txt() + ": min=" + tempMin + ", max=" + tempMax);

                // 格式化温度显示，保留1位小数以显示细微差异
                String temperatureRange = String.format(Locale.getDefault(), "%.1f°C / %.1f°C", tempMin, tempMax);
                forecastTemperatureTextView.setText(temperatureRange);

                // 获取适配器以访问整体温度范围
                ForecastAdapter adapter = (ForecastAdapter) getBindingAdapter();
                if (adapter != null) {
                    // 设置进度条的平滑渐变效果，基于上一条温度到当前温度的变化
                    // 使用新的setTemperatureData方法，让渐变的一端使用上条天气的气温数据，另一端使用当条天气的气温数据
                    temperatureProgressBar.setTemperatureData(previousTemp, tempCurr, adapter.overallMinTemp, adapter.overallMaxTemp);

                    // 设置最低和最高温度标签，显示整体温度范围
                    minTempLabel.setText(String.format(Locale.getDefault(), "最低(%.1f)", adapter.overallMinTemp));
                    maxTempLabel.setText(String.format(Locale.getDefault(), "最高(%.1f)", adapter.overallMaxTemp));
                }
            }
        }

        /**
         * 使用本地天气图标资源作为备选
         */
        private void useLocalForecastIcon(String iconCode) {
            Log.d(TAG, "Using local forecast icon for code: " + iconCode);

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

            // 设置本地图标
            forecastIconImageView.setImageResource(resourceId);
            // 确保图标颜色正确
            forecastIconImageView.setColorFilter(null);
        }

        private String formatDateTime(String dateTimeStr) {
            // 将API返回的日期时间格式（yyyy-MM-dd HH:mm:ss）转换为更友好的格式
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(dateTimeStr);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return dateTimeStr;
            }
        }
    }
}