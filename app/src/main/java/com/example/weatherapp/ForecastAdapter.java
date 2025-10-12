package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.model.ForecastWeather;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<ForecastWeather.ForecastItem> forecastItems;

    public ForecastAdapter(List<ForecastWeather.ForecastItem> forecastItems) {
        this.forecastItems = forecastItems;
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
        holder.bind(forecastItem);
    }

    @Override
    public int getItemCount() {
        return forecastItems != null ? forecastItems.size() : 0;
    }

    // 更新数据
    public void updateData(List<ForecastWeather.ForecastItem> newForecastItems) {
        this.forecastItems = newForecastItems;
        notifyDataSetChanged();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {

        private TextView forecastTimeTextView;
        private ImageView forecastIconImageView;
        private TextView forecastDescriptionTextView;
        private TextView forecastTemperatureTextView;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            forecastTimeTextView = itemView.findViewById(R.id.forecast_time);
            forecastIconImageView = itemView.findViewById(R.id.forecast_icon);
            forecastDescriptionTextView = itemView.findViewById(R.id.forecast_description);
            forecastTemperatureTextView = itemView.findViewById(R.id.forecast_temperature);
        }

        public void bind(ForecastWeather.ForecastItem forecastItem) {
            // 设置时间（格式化日期时间）
            String formattedTime = formatDateTime(forecastItem.getDt_txt());
            forecastTimeTextView.setText(formattedTime);

            // 设置天气描述
            if (forecastItem.getWeather() != null && !forecastItem.getWeather().isEmpty()) {
                String description = forecastItem.getWeather().get(0).getDescription();
                forecastDescriptionTextView.setText(description);

                // 设置天气图标
                String iconCode = forecastItem.getWeather().get(0).getIcon();
                setWeatherIcon(iconCode);
            }

            // 设置温度范围（开尔文转摄氏度）
            if (forecastItem.getMain() != null) {
                double tempMin = forecastItem.getMain().getTemp_min() - 273.15;
                double tempMax = forecastItem.getMain().getTemp_max() - 273.15;
                String temperatureRange = String.format(Locale.getDefault(), "%.0f°C / %.0f°C", tempMax, tempMin);
                forecastTemperatureTextView.setText(temperatureRange);
            }
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

        private void setWeatherIcon(String iconCode) {
            // 根据天气图标代码设置对应的图标资源
            int iconResource = R.drawable.ic_unknown;
            switch (iconCode) {
                case "01d":
                case "01n":
                    iconResource = R.drawable.ic_sunny;
                    break;
                case "02d":
                case "02n":
                    iconResource = R.drawable.ic_partly_cloudy;
                    break;
                case "03d":
                case "03n":
                case "04d":
                case "04n":
                    iconResource = R.drawable.ic_cloudy;
                    break;
                case "09d":
                case "09n":
                case "10d":
                case "10n":
                    iconResource = R.drawable.ic_rainy;
                    break;
                case "11d":
                case "11n":
                    iconResource = R.drawable.ic_stormy;
                    break;
                case "13d":
                case "13n":
                    iconResource = R.drawable.ic_snowy;
                    break;
                case "50d":
                case "50n":
                    iconResource = R.drawable.ic_foggy;
                    break;
            }
            forecastIconImageView.setImageResource(iconResource);
        }
    }
}