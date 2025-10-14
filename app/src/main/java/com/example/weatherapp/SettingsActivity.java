package com.example.weatherapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.utils.PreferencesHelper;

public class SettingsActivity extends AppCompatActivity {

    private EditText apiKeyEditText;
    private Button saveButton;
    private Button skipButton;
    private Button registerLinkButton;
    private PreferencesHelper preferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferencesHelper = new PreferencesHelper(this);
        apiKeyEditText = findViewById(R.id.api_key_edit_text);
        saveButton = findViewById(R.id.save_button);
        skipButton = findViewById(R.id.skip_button);
        registerLinkButton = findViewById(R.id.register_link_button);

        // 从缓存中加载已保存的API key（如果有）
        String cachedApiKey = preferencesHelper.getApiKey();
        if (cachedApiKey != null && !cachedApiKey.isEmpty()) {
            apiKeyEditText.setText(cachedApiKey);
        }

        // 保存API key按钮点击事件
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apiKey = apiKeyEditText.getText().toString().trim();
                if (apiKey.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "请输入API key", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 保存API key到缓存
                preferencesHelper.saveApiKey(apiKey);
                
                // 跳转到天气页面
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 跳过按钮点击事件
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接跳转到天气页面，不保存API key
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                // 添加标记，指示MainActivity跳过API Key检查
                intent.putExtra("SKIP_API_KEY_CHECK", true);
                startActivity(intent);
                finish();
            }
        });

        // 注册链接按钮点击事件
        registerLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开OpenWeatherMap注册页面
                String registrationUrl = "https://home.openweathermap.org/users/sign_up";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(registrationUrl));
                startActivity(browserIntent);
            }
        });
    }
}