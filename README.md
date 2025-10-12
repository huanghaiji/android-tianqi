# Android 天气应用程序

这是一个基于Android平台的天气应用程序，可以显示当前天气和未来天气预报。

## 功能特点

- 根据用户位置自动获取天气信息
- 显示当前温度、天气状况、湿度、风速、气压和体感温度
- 提供5天天气预报，每3小时更新一次
- 支持下拉刷新功能
- 美观的用户界面，包含天气图标和渐变色背景

## 技术栈

- Java
- Android SDK
- Retrofit 2 - 网络请求
- Gson - JSON解析
- Picasso - 图片加载（目前未使用，使用的是本地SVG图标）
- ViewModel & LiveData - 数据管理
- RecyclerView - 列表显示
- FusedLocationProviderClient - 获取位置信息
- OpenWeatherMap API - 天气数据来源

## 项目结构

```
app/src/main/
├── java/com/example/weatherapp/
│   ├── MainActivity.java       # 主活动
│   ├── ForecastAdapter.java    # 天气预报列表适配器
│   ├── model/                  # 数据模型
│   │   ├── CurrentWeather.java # 当前天气数据模型
│   │   └── ForecastWeather.java # 天气预报数据模型
│   ├── network/                # 网络相关
│   │   ├── WeatherApiService.java # API服务接口
│   │   └── WeatherRepository.java # 数据仓库
│   └── viewmodel/              # 视图模型
│       └── WeatherViewModel.java # 天气数据视图模型
└── res/
    ├── drawable/               # 图像资源
    ├── layout/                 # 布局文件
    └── values/                 # 配置值
```

## 安装和运行

1. 确保您已安装最新版本的Android Studio
2. 克隆此项目到您的本地计算机
3. 打开Android Studio并导入项目
4. 在`WeatherApiService.java`中配置您的OpenWeatherMap API密钥
5. 连接Android设备或使用模拟器运行应用程序

## API密钥配置

应用程序使用OpenWeatherMap API来获取天气数据。您需要获取一个免费的API密钥并在应用程序中配置它。

1. 访问 [OpenWeatherMap](https://openweathermap.org/) 并注册一个账户
2. 在您的账户中获取API密钥
3. 打开`app/src/main/java/com/example/weatherapp/network/WeatherApiService.java`文件
4. 将`YOUR_API_KEY_HERE`替换为您的实际API密钥

```java
// OpenWeatherMap API key
String API_KEY = "YOUR_API_KEY_HERE";
```

## 权限说明

应用程序需要以下权限：

- `INTERNET` - 用于访问天气API
- `ACCESS_FINE_LOCATION` 和 `ACCESS_COARSE_LOCATION` - 用于获取用户位置
- `ACCESS_BACKGROUND_LOCATION` (可选) - 用于在后台获取位置更新

## 注意事项

- 由于使用的是免费的OpenWeatherMap API，有请求频率限制
- 应用程序需要访问网络和位置服务才能正常工作
- 在Android 10及以上版本，需要确保应用程序有后台位置权限才能在后台更新天气信息

## License

本项目基于MIT许可证开放源代码。