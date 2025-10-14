package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherapp.model.ForecastWeather;
import com.example.weatherapp.R;
import java.util.ArrayList;
import com.example.weatherapp.ForecastAdapter;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.example.weatherapp.utils.TimeUtils;

/**
 * 按天分开展示天气预报的适配器
 */
public class DayGroupedForecastAdapter extends RecyclerView.Adapter<DayGroupedForecastAdapter.DayGroupViewHolder> {
    
    private Context context;
    private Map<String, List<ForecastWeather.ForecastItem>> groupedForecasts = new HashMap<>();
    private List<String> sortedDates = new ArrayList<>();
    
    public DayGroupedForecastAdapter(Context context) {
        this.context = context;
    }
    
    /**
     * 更新天气数据并按天分组
     */
    public void updateData(List<ForecastWeather.ForecastItem> forecastItems) {
        groupedForecasts.clear();
        sortedDates.clear();
        
        // 按日期分组
        for (ForecastWeather.ForecastItem forecast : forecastItems) {
            try {
                String dayKey = TimeUtils.extractDate(forecast.getDt_txt());
                
                if (!groupedForecasts.containsKey(dayKey)) {
                    groupedForecasts.put(dayKey, new ArrayList<>());
                    sortedDates.add(dayKey);
                }
                groupedForecasts.get(dayKey).add(forecast);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public DayGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.day_forecast_group, parent, false);
        return new DayGroupViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DayGroupViewHolder holder, int position) {
        String dateKey = sortedDates.get(position);
        List<ForecastWeather.ForecastItem> dayForecasts = groupedForecasts.get(dateKey);
        
        // 显示日期标题
        try {
            // 为了显示星期几，我们需要完整的日期时间字符串，这里我们从当天的第一个预报项中获取
            if (!dayForecasts.isEmpty()) {
                String formattedDate = TimeUtils.formatDateWithWeek(dayForecasts.get(0).getDt_txt());
                holder.dateTitleTextView.setText(formattedDate);
            } else {
                holder.dateTitleTextView.setText(dateKey);
            }
        } catch (Exception e) {
            holder.dateTitleTextView.setText(dateKey);
        }
        
        // 计算当天的温度范围
        if (!dayForecasts.isEmpty()) {
            double minTemp = Double.MAX_VALUE;
            double maxTemp = Double.MIN_VALUE;
            
            for (ForecastWeather.ForecastItem forecast : dayForecasts) {
                if (forecast.getMain() != null) {
                    double tempMin = forecast.getMain().getTemp_min() - 273.15; // 从开尔文转换为摄氏度
                    double tempMax = forecast.getMain().getTemp_max() - 273.15;
                    minTemp = Math.min(minTemp, tempMin);
                    maxTemp = Math.max(maxTemp, tempMax);
                }
            }
            
            // 显示温度范围
            holder.temperatureRangeTextView.setText(String.format("%.1f° / %.1f°", minTemp, maxTemp));
            
            // 设置小时预报适配器
            holder.hourlyForecastAdapter.updateData(dayForecasts);
        }
    }
    
    @Override
    public int getItemCount() {
        return sortedDates.size();
    }
    
    /**
     * ViewHolder用于展示按天分好的预报数据
     */
    public static class DayGroupViewHolder extends RecyclerView.ViewHolder {
        TextView dateTitleTextView;
        TextView temperatureRangeTextView;
        RecyclerView hourlyForecastRecyclerView;
        ForecastAdapter hourlyForecastAdapter;
        
        public DayGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            
            dateTitleTextView = itemView.findViewById(R.id.day_group_header);
            temperatureRangeTextView = itemView.findViewById(R.id.day_temp_range);
            hourlyForecastRecyclerView = itemView.findViewById(R.id.hourly_forecast_recycler_view);
            
            // 设置竖向滚动的小时预报RecyclerView
            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.VERTICAL, false);
            hourlyForecastRecyclerView.setLayoutManager(layoutManager);
            
            // 允许RecyclerView自适应高度
            hourlyForecastRecyclerView.setHasFixedSize(false);
            
            // 创建适配器并设置
            hourlyForecastAdapter = new ForecastAdapter(new ArrayList<>());
            hourlyForecastRecyclerView.setAdapter(hourlyForecastAdapter);
        }
    }
}