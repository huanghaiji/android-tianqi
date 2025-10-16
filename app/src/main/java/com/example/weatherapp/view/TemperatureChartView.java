package com.example.weatherapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.weatherapp.model.ForecastWeather;
import com.example.weatherapp.utils.TimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TemperatureChartView extends View {
    private Paint gridPaint;
    private Paint axisPaint;
    private Paint linePaint;
    private Paint pointPaint;
    private Paint textPaint;
    private Paint tempTextPaint;
    
    private List<Float> temperatures = new ArrayList<>();
    private List<String> times = new ArrayList<>();
    
    private float minTemp = Float.MAX_VALUE;
    private float maxTemp = Float.MIN_VALUE;
    
    private int paddingLeft = 40;
    private int paddingRight = 20;
    private int paddingTop = 20;
    private int paddingBottom = 40;
    
    public TemperatureChartView(Context context) {
        super(context);
        init();
    }
    
    public TemperatureChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public TemperatureChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // 初始化网格线画笔
        gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#2E2E2E"));
        gridPaint.setStrokeWidth(1);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAlpha(100);
        
        // 初始化坐标轴画笔
        axisPaint = new Paint();
        axisPaint.setColor(Color.parseColor("#FFFFFF"));
        axisPaint.setStrokeWidth(2);
        axisPaint.setStyle(Paint.Style.STROKE);
        
        // 初始化曲线画笔
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#4CAF50")); // 绿色曲线
        linePaint.setStrokeWidth(3);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        
        // 初始化点画笔
        pointPaint = new Paint();
        pointPaint.setColor(Color.parseColor("#2196F3")); // 蓝色数据点
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);
        
        // 初始化时间文本画笔
        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#000000")); // 改为黑色，提高可见性
        textPaint.setTextSize(20); // 增大字体大小
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        
        // 初始化温度文本画笔
        tempTextPaint = new Paint();
        tempTextPaint.setColor(Color.parseColor("#FFFFFF"));
        tempTextPaint.setTextSize(20); // 增大字体大小
        tempTextPaint.setTextAlign(Paint.Align.RIGHT);
        tempTextPaint.setAntiAlias(true);
    }
    
    public void setTemperatureData(List<ForecastWeather.ForecastItem> forecastItems) {
        temperatures.clear();
        times.clear();
        minTemp = Float.MAX_VALUE;
        maxTemp = Float.MIN_VALUE;
        
        // 只获取未来24小时的数据，或者最多8个数据点
        int count = 0;
        for (ForecastWeather.ForecastItem item : forecastItems) {
            if (count >= 8) break;
            
            // 将开尔文温度转换为摄氏度
            float tempCelsius = (float) (item.getMain().getTemp() - 273.15);
            temperatures.add(tempCelsius);
            
            // 提取时间（格式：HH:mm）
            String time = formatTime(item.getDt_txt());
            times.add(time);
            
            // 更新温度范围
            if (tempCelsius < minTemp) minTemp = tempCelsius;
            if (tempCelsius > maxTemp) maxTemp = tempCelsius;
            
            count++;
        }
        
        // 如果只有一个数据点，添加一些假数据以便绘制
        if (temperatures.size() == 1) {
            temperatures.add(temperatures.get(0) + 2);
            times.add("+");
            maxTemp = Math.max(maxTemp, temperatures.get(1));
            minTemp = Math.min(minTemp, temperatures.get(0));
        }
        
        // 增加一些边距到温度范围
        if (maxTemp > minTemp) {
            float range = maxTemp - minTemp;
            minTemp -= range * 0.1f;
            maxTemp += range * 0.1f;
        } else {
            minTemp -= 1;
            maxTemp += 1;
        }
        
        invalidate();
    }
    
    private String formatTime(String datetimeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(datetimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // 如果解析失败，返回原始时间的一部分
            String[] parts = datetimeString.split(" ");
            if (parts.length > 1) {
                return parts[1].substring(0, 5);
            }
            return datetimeString;
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // 计算图表区域
        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;
        
        // 绘制网格线
        drawGridLines(canvas, width, height, chartWidth, chartHeight);
        
        // 绘制坐标轴
        drawAxes(canvas, width, height);
        
        // 绘制曲线和点
        if (temperatures.size() > 0) {
            drawTemperatureChart(canvas, width, height, chartWidth, chartHeight);
            
            // 绘制时间标签
            drawTimeLabels(canvas, width, height, chartWidth, chartHeight);
            
            // 绘制温度标签
            drawTemperatureLabels(canvas, width, height, chartHeight);
        }
    }
    
    private void drawGridLines(Canvas canvas, int width, int height, int chartWidth, int chartHeight) {
        // 垂直网格线
        int verticalLinesCount = 4;
        for (int i = 0; i <= verticalLinesCount; i++) {
            float x = paddingLeft + (chartWidth / (float) verticalLinesCount) * i;
            canvas.drawLine(x, paddingTop, x, height - paddingBottom, gridPaint);
        }
        
        // 水平网格线
        int horizontalLinesCount = 4;
        for (int i = 0; i <= horizontalLinesCount; i++) {
            float y = height - paddingBottom - (chartHeight / (float) horizontalLinesCount) * i;
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);
        }
    }
    
    private void drawAxes(Canvas canvas, int width, int height) {
        // X轴
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, axisPaint);
        
        // Y轴
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, axisPaint);
    }
    
    private void drawTemperatureChart(Canvas canvas, int width, int height, int chartWidth, int chartHeight) {
        if (temperatures.size() < 2) return;
        
        float pointRadius = 4f;
        
        // 绘制曲线
        float xStep = chartWidth / (float) (temperatures.size() - 1);
        
        // 计算所有点的坐标
        List<PointF> points = new ArrayList<>();
        for (int i = 0; i < temperatures.size(); i++) {
            float x = paddingLeft + (xStep * i);
            float temperature = temperatures.get(i);
            float y = height - paddingBottom - ((temperature - minTemp) / (maxTemp - minTemp) * chartHeight);
            
            // 确保y值在有效范围内
            y = Math.max(paddingTop + pointRadius, Math.min(height - paddingBottom - pointRadius, y));
            
            points.add(new PointF(x, y));
        }
        
        // 逐段绘制曲线，根据温度变化趋势使用渐变色
        for (int i = 0; i < points.size() - 1; i++) {
            PointF currentPoint = points.get(i);
            PointF nextPoint = points.get(i + 1);
            
            // 确定温度变化趋势（上升还是下降）
            float currentTemp = temperatures.get(i);
            float nextTemp = temperatures.get(i + 1);
            boolean isRising = nextTemp > currentTemp;
            
            // 创建新的路径用于每一段曲线
            Path segmentPath = new Path();
            segmentPath.moveTo(currentPoint.x, currentPoint.y);
            
            // 使用简化的二次贝塞尔曲线，使曲线更平滑自然
            float controlPointX = (currentPoint.x + nextPoint.x) / 2;
            float controlPointY = (currentPoint.y + nextPoint.y) / 2;
            
            // 绘制二次贝塞尔曲线段
            segmentPath.quadTo(controlPointX, controlPointY, nextPoint.x, nextPoint.y);
            
            // 根据温度变化趋势设置渐变色
            Paint segmentPaint = new Paint(linePaint);
            
            // 定义渐变颜色
            int startColor, endColor;
            
            if (isRising) {
                // 上升波段：从绿色渐变为红色
                startColor = Color.parseColor("#4CAF50"); // 绿色起始
                endColor = Color.parseColor("#FF5722");   // 红色结束
            } else {
                // 下降波段：从红色渐变为绿色
                startColor = Color.parseColor("#FF5722"); // 红色起始
                endColor = Color.parseColor("#4CAF50");   // 绿色结束
            }
            
            // 创建线性渐变
            LinearGradient gradient = new LinearGradient(
                currentPoint.x, currentPoint.y,  // 渐变起始点
                nextPoint.x, nextPoint.y,       // 渐变结束点
                startColor, endColor,           // 起始和结束颜色
                Shader.TileMode.CLAMP           // 渐变模式
            );
            
            // 应用渐变到画笔
            segmentPaint.setShader(gradient);
            
            // 绘制当前线段
            canvas.drawPath(segmentPath, segmentPaint);
        }
        
        // 绘制数据点和温度标签（在曲线之后绘制，这样会显示在曲线前面）
        for (int i = 0; i < points.size(); i++) {
            PointF point = points.get(i);
            float temperature = temperatures.get(i);
            
            // 绘制数据点
            canvas.drawCircle(point.x, point.y, pointRadius, pointPaint);
            
            // 绘制温度标签
            String tempText = String.format("%.1f°", temperature);
            Paint tempLabelPaint = new Paint(textPaint);
            tempLabelPaint.setTextAlign(Paint.Align.CENTER);
            tempLabelPaint.setColor(Color.parseColor("#FF9800")); // 使用固定颜色
            tempLabelPaint.setTextSize(20); // 增大字体大小
            
            // 在点的上方绘制温度标签，留出一定间距
            canvas.drawText(tempText, point.x, point.y - pointRadius - 5, tempLabelPaint);
        }
    }
    
    // 用于存储坐标点的内部类
    private static class PointF {
        float x;
        float y;
        
        PointF(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private void drawTimeLabels(Canvas canvas, int width, int height, int chartWidth, int chartHeight) {
        float xStep = chartWidth / (float) (times.size() - 1);
        
        for (int i = 0; i < times.size(); i++) {
            float x = paddingLeft + (xStep * i);
            float y = height - paddingBottom + 20;
            canvas.drawText(times.get(i), x, y, textPaint);
        }
    }
    
    private void drawTemperatureLabels(Canvas canvas, int width, int height, int chartHeight) {
        // 绘制最大和最小温度
        canvas.drawText(String.format("%.1f°", maxTemp), paddingLeft - 10, paddingTop + 15, tempTextPaint);
        canvas.drawText(String.format("%.1f°", minTemp), paddingLeft - 10, height - paddingBottom - 5, tempTextPaint);
    }
}