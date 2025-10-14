package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.ForecastWeather;
import com.example.weatherapp.viewmodel.WeatherViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// import com.squareup.picasso.Picasso;
import com.example.weatherapp.utils.ImageLoader;
import com.example.weatherapp.utils.PreferencesHelper;
import com.example.weatherapp.utils.WeatherIconUtils;

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
    private TextView temperatureCityNameTextView; // 温度下方的城市名称
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
    private DayGroupedForecastAdapter dayGroupedForecastAdapter;
    private PreferencesHelper preferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 实现沉浸式设计
        setupImmersiveMode();

        // 在super.onCreate之前设置主题模式
        setThemeBasedOnTime();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化PreferencesHelper并检查API key
        preferencesHelper = new PreferencesHelper(this);
        
        // 获取Intent，检查是否包含SKIP_API_KEY_CHECK标记
        Intent intent = getIntent();
        boolean skipApiKeyCheck = intent.getBooleanExtra("SKIP_API_KEY_CHECK", false);
        
        // 如果是首次启动应用且没有API Key，则跳转到设置页面
        if (preferencesHelper.isFirstLaunch() && !preferencesHelper.hasApiKey() && !skipApiKeyCheck) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            finish();
            return;
        } else if (preferencesHelper.hasApiKey()) {
            Log.d(TAG, "Found cached API key");
        }
        
        // 如果已经启动过应用（不是首次启动），标记应用已启动
        if (preferencesHelper.isFirstLaunch() && skipApiKeyCheck) {
            preferencesHelper.setAppLaunched();
        }

        // 初始化视图组件
        cityNameTextView = findViewById(R.id.city_name);
        temperatureTextView = findViewById(R.id.temperature);
        temperatureCityNameTextView = findViewById(R.id.temperature_city_name); // 初始化温度下方的城市名称TextView
        weatherDescriptionTextView = findViewById(R.id.weather_description);
        weatherIconImageView = findViewById(R.id.weather_icon);
        humidityTextView = findViewById(R.id.humidity_value);
        windSpeedTextView = findViewById(R.id.wind_speed_value);
        pressureTextView = findViewById(R.id.pressure_value);
        feelsLikeTextView = findViewById(R.id.feels_like_value);
        lastUpdatedTextView = findViewById(R.id.last_updated);
        progressBar = findViewById(R.id.progress_bar);
        weatherContentLayout = findViewById(R.id.weather_content);
        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        forecastRecyclerView = findViewById(R.id.forecast_recycler_view);

        // 初始化RecyclerView和Adapter
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        forecastAdapter = new ForecastAdapter(new ArrayList<>());
        dayGroupedForecastAdapter = new DayGroupedForecastAdapter(this);
        forecastRecyclerView.setAdapter(dayGroupedForecastAdapter);

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
        weatherViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                weatherContentLayout.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                weatherContentLayout.setVisibility(View.VISIBLE);
            }
        });

        // 观察反向地理编码得到的城市名称
        weatherViewModel.getLocationCityName().observe(this, cityName -> {
            if (cityName != null && !cityName.isEmpty()) {
                cityNameTextView.setText(cityName);
                temperatureCityNameTextView.setText(cityName);
                // 同时更新缓存的城市名称
                preferencesHelper.saveCityName(cityName);
            }
        });

        // 观察错误状态
        weatherViewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // 检查是否有缓存的经纬度信息，如果有则预先加载
        if (preferencesHelper.hasCachedLocation()) {
            double cachedLatitude = preferencesHelper.getLatitude();
            double cachedLongitude = preferencesHelper.getLongitude();
            String cachedCityName = preferencesHelper.getCityName();
            Log.d(TAG, "Using cached location: " + cachedLatitude + ", " + cachedLongitude + ", City: " + cachedCityName);
            // 使用fetchLocationAndWeatherData确保同时获取城市名称
            weatherViewModel.fetchLocationAndWeatherData(cachedLatitude, cachedLongitude);
        }

        // 请求位置权限并获取天气数据
        requestLocationPermission();
    }

    /**
     * 设置沉浸式模式，隐藏标题栏并确保内容不与状态栏重叠
     */
    private void setupImmersiveMode() {
        // 隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 设置状态栏为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // 设置导航栏为透明
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // 控制系统UI可见性
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        // 对于Android R及以上版本，使用WindowInsetsController
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.view.WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 对于Android M到Android Q版本
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    /**
     * 根据当前时间设置应用的日/夜间模式
     * 设置在早上6点到晚上7点之间为白天模式，其他时间为夜间模式
     */
    private void setThemeBasedOnTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // 早上6点到晚上7点为白天模式，其他时间为夜间模式
        if (hour >= 6 && hour < 19) {
            // 白天模式
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Log.d(TAG, "设置为白天模式");
        } else {
            // 夜间模式
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Log.d(TAG, "设置为夜间模式");
        }
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

                        // 检查是否有缓存的位置信息
                        if (preferencesHelper.hasCachedLocation()) {
                            // 比较新获取的城市与缓存的城市是否相同
                            String cachedCityName = preferencesHelper.getCityName();
                            double cachedLatitude = preferencesHelper.getLatitude();
                            double cachedLongitude = preferencesHelper.getLongitude();

                            // 对于城市名称比较，我们需要先获取新位置对应的城市名称
                            // 由于我们还没有获取到天气数据，这里先使用经纬度进行近似比较
                            // 实际项目中可能需要更精确的地理编码比较
                            boolean isSameLocation = Math.abs(latitude - cachedLatitude) < 0.1 &&
                                    Math.abs(longitude - cachedLongitude) < 0.1;

                            if (!isSameLocation) {
                                Log.d(TAG, "Location changed significantly, fetching new location and weather data");
                                weatherViewModel.fetchLocationAndWeatherData(latitude, longitude);
                            } else {
                                Log.d(TAG, "Location is the same as cached, no need to fetch new weather data");
                                // 但仍需要更新缓存的经纬度和最后更新时间
                                if (weatherViewModel.getCurrentWeather().getValue() != null) {
                                    String cityName = weatherViewModel.getCurrentWeather().getValue().getName();
                                    preferencesHelper.saveLocation(latitude, longitude, cityName);
                                    preferencesHelper.saveLastUpdateTime(System.currentTimeMillis());
                                }
                            }
                        } else {
                            // 如果没有缓存的位置信息，则获取位置和天气数据
                            Log.d(TAG, "No cached location, fetching location and weather data");
                            weatherViewModel.fetchLocationAndWeatherData(latitude, longitude);
                        }
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
        // 降低精度要求，提高获取成功率
        long timeDelta = System.currentTimeMillis() - location.getTime();
        boolean isRecent = timeDelta < 15 * 60 * 1000; // 延长到15分钟
        boolean isAccurate = location.getAccuracy() < 50; // 放宽到50米精度
        Log.d(TAG, "Location validation - Recent: " + isRecent + ", Accurate: " + isAccurate + ", Age: " + (timeDelta / 1000) + "s, Accuracy: " + location.getAccuracy() + "m");
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

        // 设置25秒后自动停止位置更新（延长超时时间）
        locationTimeoutHandler.postDelayed(() -> {
            stopLocationUpdates();
            // 先尝试使用较低精度的位置（如果有）
            useLastKnownLocationWithLowerAccuracy();
        }, 25000);
    }

    /**
     * 当定位超时时，尝试使用较低精度要求的最后已知位置
     */
    private void useLastKnownLocationWithLowerAccuracy() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location lastLocation = null;

        // 尝试获取任何可用的位置提供者的最后已知位置
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            try {
                Location tempLocation = locationManager.getLastKnownLocation(provider);
                if (tempLocation != null) {
                    // 使用时间更接近现在的位置
                    if (lastLocation == null || tempLocation.getTime() > lastLocation.getTime()) {
                        lastLocation = tempLocation;
                    }
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when getting last known location for " + provider, e);
            }
        }

        if (lastLocation != null) {
            // 降低精度要求，使用可用的最佳位置
            long timeDelta = System.currentTimeMillis() - lastLocation.getTime();
            boolean isAcceptable = timeDelta < 30 * 60 * 1000; // 30分钟内的位置

            if (isAcceptable) {
                double latitude = lastLocation.getLatitude();
                double longitude = lastLocation.getLongitude();
                Log.d(TAG, "Using lower accuracy location: " + latitude + ", " + longitude + ", Accuracy: " + lastLocation.getAccuracy() + "m, Provider: " + lastLocation.getProvider());
                weatherViewModel.fetchLocationAndWeatherData(latitude, longitude);
                Toast.makeText(this, "使用最近的位置估算天气信息", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 如果没有可用位置，提示用户并提供默认位置作为备选
        Toast.makeText(MainActivity.this, "定位超时，请手动刷新或检查位置服务设置", Toast.LENGTH_LONG).show();

        // 使用默认位置（北京）作为最后的备选方案
        useDefaultLocation();
    }

    /**
     * 当无法获取位置时的处理方法
     * 不再使用默认位置作为兜底方案
     */
    private void useDefaultLocation() {
        Log.d(TAG, "No location available and default location fallback has been removed");
        Toast.makeText(this, "无法获取位置信息，无法加载天气数据", Toast.LENGTH_LONG).show();
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
            // 获取位置和天气数据
            weatherViewModel.fetchLocationAndWeatherData(latitude, longitude);
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
        // 检查被动定位是否开启
        boolean isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        Log.d(TAG, "Location providers status - GPS: " + isGpsEnabled + ", Network: " + isNetworkEnabled + ", Passive: " + isPassiveEnabled);

        if (!isGpsEnabled && !isNetworkEnabled && !isPassiveEnabled) {
            // 都没有开启，引导用户去设置
            Toast.makeText(this, "请开启GPS或网络定位服务以获取准确天气信息", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            Log.d(TAG, "Location settings are satisfied: GPS=" + isGpsEnabled + ", Network=" + isNetworkEnabled + ", Passive=" + isPassiveEnabled);

            // 提示用户定位提供者的状态
            StringBuilder providerStatus = new StringBuilder("当前可用定位: ");
            if (isGpsEnabled) providerStatus.append("GPS ");
            if (isNetworkEnabled) providerStatus.append("网络 ");
            if (isPassiveEnabled) providerStatus.append("被动");
            Log.d(TAG, providerStatus.toString());
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

        // 检查是否需要更新天气数据（超过5分钟）
        if (preferencesHelper.hasCachedLocation() && preferencesHelper.isWeatherDataExpired()) {
            // 从后台到前台，先检查位置是否有变化
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Checking for location changes after returning from background");
                // 尝试获取最新位置，检查是否有变化
                Location lastLocation = null;
                List<String> providers = locationManager.getProviders(true);
                for (String provider : providers) {
                    try {
                        Location tempLocation = locationManager.getLastKnownLocation(provider);
                        if (tempLocation != null) {
                            if (lastLocation == null || tempLocation.getTime() > lastLocation.getTime()) {
                                lastLocation = tempLocation;
                            }
                        }
                    } catch (SecurityException e) {
                        Log.e(TAG, "Security exception when getting last known location", e);
                    }
                }

                if (lastLocation != null) {
                    double newLatitude = lastLocation.getLatitude();
                    double newLongitude = lastLocation.getLongitude();
                    double cachedLatitude = preferencesHelper.getLatitude();
                    double cachedLongitude = preferencesHelper.getLongitude();

                    // 比较新位置与缓存位置是否有显著差异
                    boolean locationChanged = Math.abs(newLatitude - cachedLatitude) > 0.1 ||
                            Math.abs(newLongitude - cachedLongitude) > 0.1;

                    if (locationChanged) {
                        Log.d(TAG, "Location changed significantly after returning from background, fetching new location and weather data");
                        weatherViewModel.fetchLocationAndWeatherData(newLatitude, newLongitude);
                        return;
                    }
                }
            }

            // 如果位置没有变化或无法获取最新位置，则使用缓存位置更新天气数据
            double cachedLatitude = preferencesHelper.getLatitude();
            double cachedLongitude = preferencesHelper.getLongitude();
            Log.d(TAG, "Weather data expired, updating from cached location: " + cachedLatitude + ", " + cachedLongitude);
            weatherViewModel.fetchLocationAndWeatherData(cachedLatitude, cachedLongitude);
        }

        // 恢复时，如果还没有获取到位置，重新尝试
        if (weatherViewModel.getCurrentWeather().getValue() == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Resumed without weather data, trying to get location again");
                getLocation();
            } else {
                // 如果没有位置权限但有缓存的位置数据，仍然尝试更新天气
                double cachedLatitude = preferencesHelper.getLatitude();
                double cachedLongitude = preferencesHelper.getLongitude();
                if (cachedLatitude != 0 && cachedLongitude != 0) {
                    Log.d(TAG, "Resumed without location permission, using cached location: " + cachedLatitude + ", " + cachedLongitude);
                    weatherViewModel.fetchLocationAndWeatherData(cachedLatitude, cachedLongitude);
                }
            }
        }

        // 重新应用沉浸式模式设置
        setupImmersiveMode();
    }

    private void updateWeatherUI(CurrentWeather weather) {
        if (weather == null) {
            return;
        }

        // 获取并缓存经纬度信息
        double latitude = 0;
        double longitude = 0;
        if (weather.getCoord() != null) {
            latitude = weather.getCoord().getLat();
            longitude = weather.getCoord().getLon();
        }

        // 保存经纬度和更新时间，但不更新城市名称（城市名称由反向地理编码API提供）
        String currentCityName = preferencesHelper.getCityName();
        if (currentCityName == null || currentCityName.isEmpty()) {
            // 如果还没有反向地理编码的城市名称，才使用天气API返回的名称作为临时显示
            cityNameTextView.setText(weather.getName());
            temperatureCityNameTextView.setText(weather.getName());
            preferencesHelper.saveLocation(latitude, longitude, weather.getName());
        } else {
            // 否则保留反向地理编码获取的城市名称，只保存经纬度和更新时间
            preferencesHelper.saveLocation(latitude, longitude, currentCityName);
        }
        preferencesHelper.saveLastUpdateTime(System.currentTimeMillis());
        temperatureTextView.setText(String.format("%.1f°C", weather.getMain().getTemp() - 273.15));
        weatherDescriptionTextView.setText(weather.getWeather().get(0).getDescription());
        humidityTextView.setText(weather.getMain().getHumidity() + "%");
        windSpeedTextView.setText(weather.getWind().getSpeed() + " m/s");
        pressureTextView.setText(weather.getMain().getPressure() + " hPa");
        feelsLikeTextView.setText(String.format("%.1f°C", weather.getMain().getFeels_like() - 273.15));

        // 初始化刷新按钮（如果尚未初始化）
        if (refreshButton == null) {
            refreshButton = findViewById(R.id.refresh_button);
        }

        // 设置更新时间为"更新于"
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        lastUpdatedTextView.setText("更新于: " + currentTime);

        // 隐藏刷新按钮
        if (refreshButton != null) {
            refreshButton.setVisibility(View.GONE);
        }

        // 取消之前的计时器
        if (updateTimer != null) {
            updateTimer.cancel();
        }

        // 设置5分钟后切换为"最后更新"并显示刷新按钮
        updateTimer = new CountDownTimer(5 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 不需要实现
            }

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 切换为"最后更新"
                        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(preferencesHelper.getLastUpdateTime()));
                        lastUpdatedTextView.setText("最后更新: " + time);

                        // 显示刷新按钮
                        if (refreshButton != null) {
                            refreshButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        };
        updateTimer.start();

        // 根据天气条件设置图标
        String weatherIcon = weather.getWeather().get(0).getIcon();
        Log.d(TAG, "Weather icon code: " + weatherIcon);

        // 构建图标URL
        String iconUrl = "https://openweathermap.org/img/wn/" + weatherIcon + "@4x.png";
        Log.d(TAG, "Loading icon from URL: " + iconUrl);

        // 正常加载，会使用缓存
        ImageLoader.getInstance(MainActivity.this).loadImage(iconUrl, weatherIconImageView, WeatherIconUtils.getLocalWeatherIcon(weatherIcon), new ImageLoader.ImageLoadCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Weather icon loaded successfully");
                // 确保图标颜色在白天模式下正确显示
                int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (currentHour >= 6 && currentHour < 19) {
                    // 白天模式 - 确保图标不透明且颜色正确
                    weatherIconImageView.setColorFilter(null); // 清除任何颜色滤镜
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load weather icon: " + e.getMessage());
                // 如果网络加载失败，尝试使用本地图标
                onSuccess();
            }
        });
    }



    private void updateForecastUI(List<ForecastWeather.ForecastItem> forecastItems) {
        if (forecastItems != null && !forecastItems.isEmpty()) {
            // 过滤掉已过去的时间的天气预报项
            List<ForecastWeather.ForecastItem> filteredForecastItems = new ArrayList<>();
            long currentTimeMillis = System.currentTimeMillis();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (ForecastWeather.ForecastItem item : forecastItems) {
                try {
                    // 将预报时间转换为毫秒时间戳
                    Date forecastDate = dateFormat.parse(item.getDt_txt());
                    long forecastTimeMillis = forecastDate.getTime();

                    // 只保留时间未过去的预报项
                    if (forecastTimeMillis >= currentTimeMillis) {
                        filteredForecastItems.add(item);
                    } else {
                        Log.d(TAG, "Skipping past forecast: " + item.getDt_txt());
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing forecast date: " + item.getDt_txt(), e);
                    // 如果解析失败，默认添加该项
                    filteredForecastItems.add(item);
                }
            }

            Log.d(TAG, "Original forecast items: " + forecastItems.size() + ", Filtered items: " + filteredForecastItems.size());

            // 保存RecyclerView的当前滚动位置
            LinearLayoutManager layoutManager = (LinearLayoutManager) forecastRecyclerView.getLayoutManager();
            final int[] scrollPosition = {0, 0}; // 使用数组作为容器来保存位置和偏移量，使其可以在内部类中访问

            if (layoutManager != null) {
                scrollPosition[0] = layoutManager.findFirstVisibleItemPosition();
                View firstVisibleItem = layoutManager.findViewByPosition(scrollPosition[0]);
                if (firstVisibleItem != null) {
                    // 计算第一个可见项的偏移量
                    scrollPosition[1] = firstVisibleItem.getTop() - forecastRecyclerView.getPaddingTop();
                }
            }

            // 使用按天分的适配器更新数据
            dayGroupedForecastAdapter.updateData(filteredForecastItems);

            // 恢复RecyclerView的滚动位置，确保用户手动滑动的位置保持不变
            if (layoutManager != null && forecastRecyclerView.getAdapter() != null &&
                    forecastRecyclerView.getAdapter().getItemCount() > scrollPosition[0]) {
                // 保存layoutManager的引用
                final LinearLayoutManager finalLayoutManager = layoutManager;
                // 使用post确保RecyclerView已经完成布局更新
                forecastRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        finalLayoutManager.scrollToPositionWithOffset(scrollPosition[0], scrollPosition[1]);
                    }
                });
            }
        }
    }

    // 定时器，用于5分钟后切换文本和显示刷新按钮
    private CountDownTimer updateTimer;
    private View refreshButton;

    // 刷新天气数据的方法
    public void refreshWeather(View view) {
        // 隐藏刷新按钮
        if (refreshButton != null) {
            refreshButton.setVisibility(View.GONE);
        }

        // 更新最后更新时间文本为"更新于"
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        lastUpdatedTextView.setText("更新于: " + currentTime);
        preferencesHelper.saveLastUpdateTime(System.currentTimeMillis());

        // 直接使用缓存的位置刷新天气数据，不重新请求位置权限
        if (preferencesHelper.hasCachedLocation()) {
            double cachedLatitude = preferencesHelper.getLatitude();
            double cachedLongitude = preferencesHelper.getLongitude();
            Log.d(TAG, "Refreshing weather data with cached location: " + cachedLatitude + ", " + cachedLongitude);
            weatherViewModel.fetchLocationAndWeatherData(cachedLatitude, cachedLongitude);
        } else {
            // 如果没有缓存位置，才请求位置权限
            requestLocationPermission();
        }
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