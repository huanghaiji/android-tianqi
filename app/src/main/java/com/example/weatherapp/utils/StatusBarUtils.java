package com.example.weatherapp.utils;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;

/**
 * 状态栏工具类，提供获取状态栏高度和设置沉浸式模式的功能
 */
public class StatusBarUtils {
    
    /**
     * 获取状态栏高度
     * @param context 上下文
     * @return 状态栏高度（像素）
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    /**
     * 设置沉浸式模式，隐藏标题栏并确保内容不与状态栏重叠
     * @param activity AppCompatActivity实例
     */
    public static void setupImmersiveMode(AppCompatActivity activity) {
        // 隐藏标题栏
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().hide();
        }

        // 设置状态栏为透明
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);

        // 设置导航栏为透明
        activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // 控制系统UI可见性
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        // 对于Android R及以上版本，使用WindowInsetsController
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = activity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 对于Android M到Android Q版本
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }
    
    /**
     * 将状态栏高度应用到顶部布局
     * @param activity AppCompatActivity实例
     * @param topBarId 顶部布局的资源ID
     */
    public static void applyStatusBarPadding(AppCompatActivity activity, int topBarId) {
        // 获取状态栏高度
        int statusBarHeight = getStatusBarHeight(activity);
        
        // 在Activity创建后才执行此操作，确保视图已加载
        if (statusBarHeight > 0 && activity.findViewById(topBarId) != null) {
            RelativeLayout topBar = activity.findViewById(topBarId);
            // 设置顶部margin为状态栏高度
            ((android.view.ViewGroup.MarginLayoutParams) topBar.getLayoutParams()).topMargin = statusBarHeight;
            topBar.setLayoutParams(topBar.getLayoutParams());
        }
    }
}