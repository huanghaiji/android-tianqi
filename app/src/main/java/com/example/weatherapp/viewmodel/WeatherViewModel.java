package com.example.weatherapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.ForecastWeather;
import com.example.weatherapp.model.ReverseGeocodingResponse;
import com.example.weatherapp.network.WeatherApiService;
import com.example.weatherapp.network.WeatherRepository;
import com.example.weatherapp.utils.PreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherViewModel extends AndroidViewModel {
    private static final String TAG = "WeatherViewModel";
    private WeatherRepository weatherRepository;
    private PreferencesHelper preferencesHelper;

    private MutableLiveData<CurrentWeather> currentWeather = new MutableLiveData<>();
    private MutableLiveData<ForecastWeather> forecastWeather = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<String> locationCityName = new MutableLiveData<>();

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        weatherRepository = WeatherRepository.getInstance();
        preferencesHelper = new PreferencesHelper(application);
        
        // 设置API key到WeatherRepository
        String apiKey = preferencesHelper.getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            weatherRepository.setApiKey(apiKey);
        }
    }

    public LiveData<CurrentWeather> getCurrentWeather() {
        return currentWeather;
    }

    public LiveData<ForecastWeather> getForecastWeather() {
        return forecastWeather;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
    
    // 获取反向地理编码得到的城市名称
    public LiveData<String> getLocationCityName() {
        return locationCityName;
    }

    // 直接获取天气数据（添加参数控制是否设置加载状态）
    public void fetchWeatherData(double latitude, double longitude) {
        // 默认为true，表示初始加载时设置加载状态
        fetchWeatherData(latitude, longitude, true);
    }
    
    // 重载方法，允许控制是否设置加载状态
    public void fetchWeatherData(double latitude, double longitude, boolean setLoadingState) {
        if (setLoadingState) {
            isLoading.setValue(true);
        }
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

    // 先获取位置信息，再获取天气数据（新方法）
    public void fetchLocationAndWeatherData(final double latitude, final double longitude) {
        // 不设置isLoading为true，保持天气页面可见
        error.setValue(null);

        // 先使用正确的反向地理编码API获取位置信息
        weatherRepository.getReverseGeocodingInfo(latitude, longitude).enqueue(new Callback<ReverseGeocodingResponse[]>() {
            @Override
            public void onResponse(Call<ReverseGeocodingResponse[]> call, Response<ReverseGeocodingResponse[]> response) {
                if (response.isSuccessful() && response.body() != null && response.body().length > 0) {
                    // 获取到了真实的位置信息
                    ReverseGeocodingResponse locationInfo = response.body()[0];
                    String realCityName = locationInfo.getCityName();
                    Log.d(TAG, "Got real location info: " + realCityName + ", " + locationInfo.getCountry());
                    
                    // 更新城市名称
                    locationCityName.setValue(realCityName);
                    
                    // 然后获取天气数据，不设置加载状态
                    fetchWeatherData(latitude, longitude, false);
                } else {
                    Log.e(TAG, "Reverse geocoding error: " + response.message());
                    // 即使获取位置信息失败，也继续获取天气数据，不设置加载状态
                    fetchWeatherData(latitude, longitude, false);
                }
            }

            @Override
            public void onFailure(Call<ReverseGeocodingResponse[]> call, Throwable t) {
                Log.e(TAG, "Reverse geocoding network error: " + t.getMessage());
                // 即使获取位置信息失败，也继续获取天气数据，不设置加载状态
                fetchWeatherData(latitude, longitude, false);
            }
        });
    }
}