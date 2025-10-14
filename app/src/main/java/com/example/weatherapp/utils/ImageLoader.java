package com.example.weatherapp.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.example.weatherapp.R;

import java.lang.ref.WeakReference;

/**
 * 图片加载工具类，封装了Glide的使用
 */
public class ImageLoader {

    private static volatile ImageLoader instance;
    private final Context context;

    /**
     * 图片加载回调接口
     */
    public interface ImageLoadCallback {
        void onSuccess();

        void onError(Exception e);
    }

    private ImageLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 获取单例实例
     *
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
     *
     * @param imageUrl  图片URL
     * @param imageView 目标ImageView
     * @param callback  加载回调
     */
    public void loadImage(String imageUrl, ImageView imageView,int errorResId, final ImageLoadCallback callback) {
        RequestBuilder<Drawable> requestBuilder = Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 默认缓存策略
                .error(errorResId)
                .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                        callback.onError(e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        callback.onSuccess();
                        return false;
                    }
                });

        requestBuilder.into(imageView);
    }
}