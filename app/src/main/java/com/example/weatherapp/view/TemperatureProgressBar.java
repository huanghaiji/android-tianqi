package com.example.weatherapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weatherapp.R;

/**
 * 自定义温度进度条控件
 * 根据温度变化方向显示不同的渐变效果
 */
public class TemperatureProgressBar extends View {
    private static final String TAG = "TemperatureProgressBar";

    // 默认尺寸和颜色
    private static final int DEFAULT_HEIGHT = 8;
    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_BACKGROUND_COLOR = Color.GRAY;
    private static final int DEFAULT_START_COLOR = Color.GREEN; // 低温颜色
    private static final int DEFAULT_END_COLOR = Color.RED;     // 高温颜色

    // 属性
    private int mHeight = dpToPx(DEFAULT_HEIGHT);
    private int mBackgroundColor = DEFAULT_BACKGROUND_COLOR;
    private int mStartColor = DEFAULT_START_COLOR;
    private int mEndColor = DEFAULT_END_COLOR;

    // 存储上一条和当前的温度值
    private double mPreviousTemp = Double.NaN;
    private double mCurrentTemp = 0;
    private double mOverallMinTemp = -10;
    private double mOverallMaxTemp = 40;

    // 绘制相关
    private Paint mBackgroundPaint;
    private Paint mProgressPaint;
    private RectF mBackgroundRect;
    private RectF mProgressRect;
    private float mCornerRadius;

    public TemperatureProgressBar(Context context) {
        super(context);
        init(null);
    }

    public TemperatureProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TemperatureProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        // 加载自定义属性
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TemperatureProgressBar);
            mBackgroundColor = a.getColor(R.styleable.TemperatureProgressBar_backgroundColor,
                    DEFAULT_BACKGROUND_COLOR);
            mStartColor = a.getColor(R.styleable.TemperatureProgressBar_startColor,
                    DEFAULT_START_COLOR);
            mEndColor = a.getColor(R.styleable.TemperatureProgressBar_endColor,
                    DEFAULT_END_COLOR);

            a.recycle();
        }

        // 初始化画笔
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.FILL);

        // 初始化矩形
        mBackgroundRect = new RectF();
        mProgressRect = new RectF();

        // 设置圆角半径为高度的一半
        mCornerRadius = (float) mHeight / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 确定宽度
        int width = MeasureSpec.getSize(widthMeasureSpec);

        // 确定高度
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
            // 如果高度是精确值，使用它
            mHeight = height;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            // 如果高度有上限，取最小值
            mHeight = Math.min(height, dpToPx(DEFAULT_HEIGHT));
        } else {
            // 否则使用默认高度
            mHeight = dpToPx(DEFAULT_HEIGHT);
        }

        // 更新圆角半径
        mCornerRadius = mHeight / 2;

        // 设置测量尺寸
        setMeasuredDimension(width, mHeight);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int a1 = canvas.save();
        // 计算背景矩形
        mBackgroundRect.set(0, 0, getWidth(), mHeight);

        // 绘制背景
        canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mBackgroundPaint);

        // 计算进度宽度
        float progressWidth = getWidth();

        // 计算进度矩形
        mProgressRect.set(0, 0, progressWidth, mHeight);
        if (Double.isNaN(mPreviousTemp)) {
            mPreviousTemp = mOverallMinTemp;
        }
        // 然后在上一条温度和当前温度之间绘制渐变效果
        if (mPreviousTemp != mCurrentTemp && mOverallMaxTemp > mOverallMinTemp) {
            // 确保温度在有效范围内
            double clampedPrevTemp = Math.max(mOverallMinTemp, Math.min(mOverallMaxTemp, mPreviousTemp));
            double clampedCurrTemp = Math.max(mOverallMinTemp, Math.min(mOverallMaxTemp, mCurrentTemp));

            // 计算在上限和下限中的相对位置
            float prevTempPos = (float) ((clampedPrevTemp - mOverallMinTemp) / (mOverallMaxTemp - mOverallMinTemp) * getWidth());
            float currTempPos = (float) ((clampedCurrTemp - mOverallMinTemp) / (mOverallMaxTemp - mOverallMinTemp) * getWidth());

            // 确保prevTempPos不超过当前进度的位置
            prevTempPos = Math.min(prevTempPos, progressWidth);

            // 计算渐变区域的矩形
            float gradientLeft = Math.min(prevTempPos, currTempPos);
            float gradientRight = Math.max(prevTempPos, currTempPos);

            // 确保渐变区域不会超出当前进度
            gradientRight = Math.min(gradientRight, progressWidth);

            if (gradientLeft == gradientRight) {
                if (mPreviousTemp < mCurrentTemp) {
                    gradientRight = gradientLeft + mCornerRadius*2;
                } else {
                    gradientLeft = gradientRight - mCornerRadius*2;
                }
            }
            // 创建渐变区域矩形
            RectF gradientRect = new RectF(gradientLeft, 0, gradientRight, mHeight);

            // 根据温度高低设置渐变颜色方向
            LinearGradient gradient;
            if (mPreviousTemp < mCurrentTemp) {
                // 温度上升：从低温色渐变到高温色
                gradient = new LinearGradient(
                        gradientLeft, 0, gradientRight, 0,
                        mStartColor, mEndColor,
                        Shader.TileMode.CLAMP);
                mProgressPaint.setShader(gradient);
            } else {
                // 温度下降：从高温色渐变到低温色
                gradient = new LinearGradient(
                        gradientLeft, 0, gradientRight, 0,
                        mEndColor, mStartColor,
                        Shader.TileMode.CLAMP);
                mProgressPaint.setShader(gradient);
            }
            // 绘制渐变区域
            canvas.drawRoundRect(gradientRect, mCornerRadius, mCornerRadius, mProgressPaint);

            canvas.restoreToCount(a1);
        }
    }


    // 设置背景颜色
    public void setBackgroundColor(int color) {
        this.mBackgroundColor = color;
        mBackgroundPaint.setColor(color);
        invalidate();
    }

    // 设置渐变颜色
    public void setGradientColors(int startColor, int endColor) {
        this.mStartColor = startColor;
        this.mEndColor = endColor;
        invalidate();
    }

    // 设置温度数据
    public void setTemperatureData(double previousTemp, double currentTemp, double overallMinTemp, double overallMaxTemp) {
        this.mPreviousTemp = previousTemp;
        this.mCurrentTemp = currentTemp;
        this.mOverallMinTemp = overallMinTemp;
        this.mOverallMaxTemp = overallMaxTemp;

        invalidate();
    }

    // dp转px
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}