package com.example.trafficsignrecognition.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {

    private List<Rect> detectedRect = new ArrayList<>();
    private List<int []> positions = new ArrayList<>();
    private Paint paint;
    private Paint textPaint;

    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        // 用于绘制文本的Paint
        textPaint = new Paint();
        textPaint.setColor(0xFFFF0000);
        textPaint.setTextSize(15); // 设置字体大小
        textPaint.setStyle(Paint.Style.FILL);
    }

    // 更新检测框的列表并刷新视图
    public void setDetectedRect(List<Rect> rect, List<int[]> position) {
        detectedRect = rect;
        positions = position;
        invalidate();  // 刷新视图
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制所有的检测框
        for (int i = 0; i < detectedRect.size(); i++) {
            canvas.drawRect(detectedRect.get(i), paint);
            // 获取要绘制的标签文本
            String label = String.valueOf(i + 1);
            // 计算文本的宽度
            float textWidth = textPaint.measureText(label);
            // 在矩形左方绘制标签
            canvas.drawText(label, positions.get(i)[0] - textWidth - 5, (float) (positions.get(i)[1] + positions.get(i)[2]) / 2 + textPaint.getTextSize() / 2, textPaint);
        }
    }
}

