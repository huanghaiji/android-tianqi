package com.example.weatherapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.ForecastWeather;
import com.example.weatherapp.network.WeatherApiService;
import com.example.weatherapp.network.WeatherRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherViewModel extends AndroidViewModel {
    private static final String TAG = "WeatherViewModel";
    private WeatherRepository weatherRepository;

    private MutableLiveData<CurrentWeather> currentWeather = new MutableLiveData<>();
    private MutableLiveData<ForecastWeather> forecastWeather = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        weatherRepository = WeatherRepository.getInstance();
    }

    public LiveData<CurrentWeather> getCurrentWeather() {
        return currentWeather;
    }

    public LiveData<ForecastWeather> getForecastWeather() {
        return forecastWeather;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchWeatherData(double latitude, double longitude) {
        isLoading.setValue(true);
        error.setValue(null);

        // 获取当前天气数据
        weatherRepository.getCurrentWeather(latitude, longitude).enqueue(new Callback<CurrentWeather>() {
            @Override
            public void onResponse(Call<CurrentWeather> call, Response<CurrentWeather> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentWeather.setValue(response.body());
                } else {
                    error.setValue("获取当前天气失败: " + response.message());
                    Log.e(TAG, "Current weather error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CurrentWeather> call, Throwable t) {
                error.setValue("网络错误: " + t.getMessage());
                Log.e(TAG, "Current weather network error: " + t.getMessage());
            }
        });

        // 获取天气预报数据
        weatherRepository.getForecastWeather(latitude, longitude).enqueue(new Callback<ForecastWeather>() {
            @Override
            public void onResponse(Call<ForecastWeather> call, Response<ForecastWeather> response) {
                if (response.isSuccessful() && response.body() != null) {
                    forecastWeather.setValue(response.body());
                } else {
                    error.setValue("获取天气预报失败: " + response.message());
                    Log.e(TAG, "Forecast weather error: " + response.message());
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Call<ForecastWeather> call, Throwable t) {
                error.setValue("网络错误: " + t.getMessage());
                Log.e(TAG, "Forecast weather network error: " + t.getMessage());
                isLoading.setValue(false);
            }
        });
    }
}