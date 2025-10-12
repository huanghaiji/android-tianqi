package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.ForecastWeather;
import com.example.weatherapp.viewmodel.WeatherViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WeatherApp";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    
    // 添加位置请求相关常量
    private static final int LOCATION_REQUEST_INTERVAL = 10000; // 10秒
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 5000; // 5秒

    private WeatherViewModel weatherViewModel;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Handler locationTimeoutHandler;

    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private TextView weatherDescriptionTextView;
    private ImageView weatherIconImageView;
    private TextView humidityTextView;
    private TextView windSpeedTextView;
    private TextView pressureTextView;
    private TextView feelsLikeTextView;
    private TextView lastUpdatedTextView;
    private ProgressBar progressBar;
    private View weatherContentLayout;
    private RecyclerView forecastRecyclerView;
    private ForecastAdapter forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图组件
        cityNameTextView = findViewById(R.id.city_name);
        temperatureTextView = findViewById(R.id.temperature);
        weatherDescriptionTextView = findViewById(R.id.weather_description);
        weatherIconImageView = findViewById(R.id.weather_icon);
        humidityTextView = findViewById(R.id.humidity_value);
        windSpeedTextView = findViewById(R.id.wind_speed_value);
        pressureTextView = findViewById(R.id.pressure_value);
        feelsLikeTextView = findViewById(R.id.feels_like_value);
        lastUpdatedTextView = findViewById(R.id.last_updated);
        progressBar = findViewById(R.id.progress_bar);
        weatherContentLayout = findViewById(R.id.weather_content);
        forecastRecyclerView = findViewById(R.id.forecast_recycler_view);
        
        // 初始化RecyclerView和Adapter
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        forecastAdapter = new ForecastAdapter(new ArrayList<>());
        forecastRecyclerView.setAdapter(forecastAdapter);

        // 初始化ViewModel和LocationManager
        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationTimeoutHandler = new Handler(Looper.getMainLooper());
        
        // 初始化位置监听器
        initLocationListener();

        // 观察当前天气数据
        weatherViewModel.getCurrentWeather().observe(this, currentWeather -> {
            if (currentWeather != null) {
                updateWeatherUI(currentWeather);
            }
        });

        // 观察预测天气数据
        weatherViewModel.getForecastWeather().observe(this, forecastWeather -> {
            if (forecastWeather != null) {
                // 实现未来天气预测的UI更新
                Log.d(TAG, "Forecast data received: " + forecastWeather.getList().size() + " entries");
                updateForecastUI(forecastWeather.getList());
            }
        });

        // 观察加载状态
        weatherViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                weatherContentLayout.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                weatherContentLayout.setVisibility(View.VISIBLE);
            }
        });

        // 观察错误状态
        weatherViewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // 请求位置权限并获取天气数据
        requestLocationPermission();
    }

    private void initLocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (location != null) {
                    // 验证位置精度和时间
                    if (isLocationAccurate(location)) {
                        // 获取到位置后停止位置更新
                        stopLocationUpdates();
                        
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d(TAG, "Location from request: " + latitude + ", " + longitude + ", Accuracy: " + location.getAccuracy() + "m, Provider: " + location.getProvider());
                        // 获取天气数据
                        weatherViewModel.fetchWeatherData(latitude, longitude);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // 位置提供者状态变化时的处理
                Log.d(TAG, "Location provider status changed: " + provider + ", status: " + status);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                // 位置提供者启用时的处理
                Log.d(TAG, "Location provider enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                // 位置提供者禁用时的处理
                Log.d(TAG, "Location provider disabled: " + provider);
                Toast.makeText(MainActivity.this, "请启用" + provider + "定位服务以获取天气信息", Toast.LENGTH_LONG).show();
            }
        };
    }

    /**
     * 验证位置是否足够准确
     */
    private boolean isLocationAccurate(Location location) {
        // 检查位置是否足够新（10分钟内）且足够准确（精度小于20米）
        long timeDelta = System.currentTimeMillis() - location.getTime();
        boolean isRecent = timeDelta < 10 * 60 * 1000; // 10分钟
        boolean isAccurate = location.getAccuracy() < 20; // 20米精度
        Log.d(TAG, "Location validation - Recent: " + isRecent + ", Accurate: " + isAccurate + ", Age: " + (timeDelta/1000) + "s, Accuracy: " + location.getAccuracy() + "m");
        return location != null && isRecent && isAccurate;
    }

    private void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
                Log.d(TAG, "Location updates stopped");
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when stopping location updates", e);
            }
        }
        
        // 取消超时处理
        if (locationTimeoutHandler != null) {
            locationTimeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        // 尝试使用GPS_PROVIDER获取定位
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
                                                     LOCATION_REQUEST_INTERVAL, 
                                                     10, // 最小距离10米
                                                     locationListener);
                Log.d(TAG, "GPS location updates started");
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when starting GPS location updates", e);
            }
        }
        
        // 同时尝试使用NETWORK_PROVIDER获取定位
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
                                                     LOCATION_REQUEST_FASTEST_INTERVAL, 
                                                     10, // 最小距离10米
                                                     locationListener);
                Log.d(TAG, "Network location updates started");
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when starting network location updates", e);
            }
        }
        
        // 设置15秒后自动停止位置更新（以防长时间获取不到位置）
        locationTimeoutHandler.postDelayed(() -> {
            stopLocationUpdates();
            Toast.makeText(MainActivity.this, "定位超时，请检查位置服务设置", Toast.LENGTH_LONG).show();
        }, 15000);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // 检查设备位置设置是否已启用
        checkLocationSettings();

        // 首先尝试获取最后已知位置
        Location lastLocation = null;
        
        // 优先尝试GPS定位
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    Log.d(TAG, "Last known GPS location found");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when getting last GPS location", e);
            }
        }
        
        // 如果没有GPS位置，尝试网络位置
        if (lastLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            try {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastLocation != null) {
                    Log.d(TAG, "Last known network location found");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when getting last network location", e);
            }
        }
        
        // 尝试被动定位提供者
        if (lastLocation == null && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            try {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (lastLocation != null) {
                    Log.d(TAG, "Last known passive location found");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when getting last passive location", e);
            }
        }
        
        if (lastLocation != null && isLocationAccurate(lastLocation)) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            Log.d(TAG, "Location: " + latitude + ", " + longitude + ", Provider: " + lastLocation.getProvider());
            // 获取天气数据
            weatherViewModel.fetchWeatherData(latitude, longitude);
        } else {
            Log.d(TAG, "Last location is null or not accurate, requesting location updates");
            Toast.makeText(MainActivity.this, "正在请求位置信息，请稍候...", Toast.LENGTH_SHORT).show();
            // 如果最后已知位置为null或不够准确，请求位置更新
            startLocationUpdates();
        }
    }

    /**
     * 检查设备位置设置是否已启用
     */
    private void checkLocationSettings() {
        // 检查GPS是否开启
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 检查网络定位是否开启
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
        if (!isGpsEnabled && !isNetworkEnabled) {
            // 都没有开启，引导用户去设置
            Toast.makeText(this, "请开启GPS或网络定位服务", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            Log.d(TAG, "Location settings are satisfied: GPS=" + isGpsEnabled + ", Network=" + isNetworkEnabled);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停时停止位置更新
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 恢复时，如果还没有获取到位置，重新尝试
        if (weatherViewModel.getCurrentWeather().getValue() == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Resumed without weather data, trying to get location again");
                getLocation();
            }
        }
    }

    private void updateWeatherUI(CurrentWeather weather) {
        if (weather == null) {
            return;
        }

        // 更新UI组件
        cityNameTextView.setText(weather.getName());
        temperatureTextView.setText(String.format("%.1f°C", weather.getMain().getTemp() - 273.15));
        weatherDescriptionTextView.setText(weather.getWeather().get(0).getDescription());
        humidityTextView.setText(weather.getMain().getHumidity() + "%");
        windSpeedTextView.setText(weather.getWind().getSpeed() + " m/s");
        pressureTextView.setText(weather.getMain().getPressure() + " hPa");
        feelsLikeTextView.setText(String.format("%.1f°C", weather.getMain().getFeels_like() - 273.15));

        // 更新最后更新时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        lastUpdatedTextView.setText("最后更新: " + currentTime);

        // 根据天气条件设置图标（简单实现，实际项目中可以使用更复杂的图标系统）
        String weatherIcon = weather.getWeather().get(0).getIcon();
        // 这里可以使用Picasso加载天气图标
        // Picasso.get().load("http://openweathermap.org/img/wn/" + weatherIcon + "@2x.png").into(weatherIconImageView);
        // 为了演示，根据天气条件设置不同的图标
        switch (weatherIcon) {
            case "01d":
            case "01n":
                weatherIconImageView.setImageResource(R.drawable.ic_sunny);
                break;
            case "02d":
            case "02n":
                weatherIconImageView.setImageResource(R.drawable.ic_partly_cloudy);
                break;
            case "03d":
            case "03n":
            case "04d":
            case "04n":
                weatherIconImageView.setImageResource(R.drawable.ic_cloudy);
                break;
            case "09d":
            case "09n":
            case "10d":
            case "10n":
                weatherIconImageView.setImageResource(R.drawable.ic_rainy);
                break;
            case "11d":
            case "11n":
                weatherIconImageView.setImageResource(R.drawable.ic_stormy);
                break;
            case "13d":
            case "13n":
                weatherIconImageView.setImageResource(R.drawable.ic_snowy);
                break;
            case "50d":
            case "50n":
                weatherIconImageView.setImageResource(R.drawable.ic_foggy);
                break;
            default:
                weatherIconImageView.setImageResource(R.drawable.ic_unknown);
        }
    }

    private void updateForecastUI(List<ForecastWeather.ForecastItem> forecastItems) {
        if (forecastItems != null && !forecastItems.isEmpty()) {
            forecastAdapter.updateData(forecastItems);
        }
    }
    
    // 刷新天气数据的方法
    public void refreshWeather(View view) {
        requestLocationPermission();
    }

    // 请求位置权限
    private void requestLocationPermission() {
        // 检查是否已经获取了精确位置权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission already granted");
            // 已经获得权限，直接获取位置
            getLocation();
        } else {
            Log.d(TAG, "Requesting location permission");
            // 请求位置权限
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // 检查是否有任何权限被授予
            boolean hasAnyPermission = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    hasAnyPermission = true;
                    break;
                }
            }
            
            if (hasAnyPermission) {
                // 用户授予了至少一个权限，尝试获取位置
                Log.d(TAG, "Location permission granted");
                getLocation();
            } else {
                // 用户拒绝了所有权限
                Log.w(TAG, "Location permission denied");
                Toast.makeText(this, "需要位置权限来获取天气信息", Toast.LENGTH_LONG).show();
                // 显示请求权限的对话框解释为什么需要权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "天气应用需要访问您的位置以提供准确的天气预报", Toast.LENGTH_LONG).show();
                    // 再次请求权限
                    requestLocationPermission();
                }
            }
        }
    }
}