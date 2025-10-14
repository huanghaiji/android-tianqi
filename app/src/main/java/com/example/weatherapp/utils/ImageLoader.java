package com.example.weatherapp.utils;

import android.content.Context;
import android.widget.ImageView;

import com.example.weatherapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

/**
 * 图片加载工具类，封装了Picasso的使用
 */
public class ImageLoader {
    
    private static final String TAG = "ImageLoader";
    private static volatile ImageLoader instance;
    private final Context context;
    private final Picasso picassoInstance;
    
    /**
     * 图片加载回调接口
     */
    public interface ImageLoadCallback {
        void onSuccess();
        void onError(Exception e);
    }
    
    private ImageLoader(Context context) {
        this.context = context.getApplicationContext();
        
        // 使用默认配置的Picasso实例
        // Picasso默认已经启用了内存缓存和磁盘缓存
        picassoInstance = Picasso.get();
    }
    
    /**
     * 获取单例实例
     * @param context 上下文
     * @return ImageLoader实例
     */
    public static ImageLoader getInstance(Context context) {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * 加载网络图片到ImageView
     * @param imageUrl 图片URL
     * @param imageView 目标ImageView
     * @param callback 加载回调
     */
    public void loadImage(String imageUrl, ImageView imageView, final ImageLoadCallback callback) {
        picassoInstance
            .load(imageUrl)
            .error(R.drawable.ic_unknown) // 设置加载失败的错误图
            // 默认行为就是优先使用缓存，不需要额外设置缓存策略
            .into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }
                
                @Override
                public void onError(Exception e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
            });
    }
    
    /**
     * 加载网络图片到ImageView（无回调）
     * @param imageUrl 图片URL
     * @param imageView 目标ImageView
     */
    public void loadImage(String imageUrl, ImageView imageView) {
        loadImage(imageUrl, imageView, null);
    }
    
    /**
     * 清除特定图片的缓存
     * @param imageUrl 图片URL
     */
    public void clearImageCache(String imageUrl) {
        picassoInstance.invalidate(imageUrl);
    }
    
    /**
     * 清除特定文件的缓存
     * @param file 文件对象
     */
    public void clearImageCache(java.io.File file) {
        picassoInstance.invalidate(file);
    }
    
    /**
     * 清除特定URI的缓存
     * @param uri URI对象
     */
    public void clearImageCache(android.net.Uri uri) {
        picassoInstance.invalidate(uri);
    }
    
    /**
     * 清除所有内存缓存
     */
    public void clearMemoryCache() {
        picassoInstance.shutdown();
    }
    
    /**
     * 加载图片但跳过内存缓存（适用于需要刷新的图片）
     * @param imageUrl 图片URL
     * @param imageView 目标ImageView
     * @param callback 加载回调
     */
    public void loadImageSkipCache(String imageUrl, ImageView imageView, final ImageLoadCallback callback) {
        picassoInstance
            .load(imageUrl)
            .error(R.drawable.ic_unknown) // 设置加载失败的错误图
            .memoryPolicy(com.squareup.picasso.MemoryPolicy.NO_CACHE) // 不使用内存缓存
            .networkPolicy(com.squareup.picasso.NetworkPolicy.NO_CACHE) // 不使用网络缓存
            .into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }
                
                @Override
                public void onError(Exception e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
            });
    }
}