package com.example.trafficsignrecognition.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.trafficsignrecognition.R;

import java.util.ArrayList;
import java.util.List;

public class CustomSeekbar extends View {
    private final int defaultLineColor = Color.rgb(33, 33, 33);
    private int defaultLineWidth;
    private int defaultCircleRadius;
    private final int defaultPosition = 2;

    // 一共有多少格
    private int max = 5;
    // 线条颜色
    private int lineColor;
    // 线条粗细
    private int lineWidth;
    // 突出部分的线条高度
    private int lineHeight;
    // 圆半径
    private int circleRadius;
    private int circleColor;
    // 一段的宽度，根据总宽度和总格数计算得来
    private int itemWidth;
    // 控件的宽高
    private int height;
    private int width;
    // 当前所在位置
    private int currentProgress = defaultPosition;
    // 画笔
    private Paint mLinePaint;
    private Paint mCirclePaint;
    private Paint mTextPaint;
    // 滑动过程中x坐标
    private float currentX = 0;
    // 有效数据点
    private final List<Point> points = new ArrayList<>();

    private float circleY;

    public CustomSeekbar(Context context) {
        this(context, null);
    }

    public CustomSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // initDefault
        defaultLineWidth = dp2px(context, 2);
        defaultCircleRadius = dp2px(context, 35);

        lineColor = Color.rgb(33, 33, 33);
        lineWidth = dp2px(context, 2);
        circleColor = Color.WHITE;

        // initCustomAttrs
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SetTextSizeView);
        final int N = typedArray.getIndexCount();
        for (int i = 0; i < N; i++) {
            initCustomAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();

        // 初始化画笔
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(lineColor);
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mLinePaint.setStrokeWidth(lineWidth);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(circleColor);
        mCirclePaint.setStyle(Paint.Style.FILL);
        // 设置阴影效果
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mCirclePaint.setShadowLayer(2, 0, 0, Color.rgb(33, 33, 33));

        // 初始化文字画笔
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);  // 设置文字颜色为黑色
        mTextPaint.setTextAlign(Paint.Align.CENTER);  // 设置文字对齐方式为居中
    }

    private void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.SetTextSizeView_lineColor) {
            lineColor = typedArray.getColor(attr, defaultLineColor);
        } else if (attr == R.styleable.SetTextSizeView_circleColor) {
            int defaultCircleColor = Color.WHITE;
            circleColor = typedArray.getColor(attr, defaultCircleColor);
        } else if (attr == R.styleable.SetTextSizeView_lineWidth) {
            lineWidth = typedArray.getDimensionPixelSize(attr, defaultLineWidth);
        } else if (attr == R.styleable.SetTextSizeView_circleRadius) {
            circleRadius = typedArray.getDimensionPixelSize(attr, defaultCircleRadius);
        } else if (attr == R.styleable.SetTextSizeView_totalCount) {
            int defaultMax = 5;
            max = typedArray.getInteger(attr, defaultMax);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        height = h;
        width = w;
        circleY = (float) height / 2;
        lineHeight = height / 8;
        // 横线宽度是总宽度-2个圆的半径
        itemWidth = (w - 2 * circleRadius) / max;
        // 把可点击点保存起来
        for (int i = 0; i <= max; i++) {
            points.add(new Point(circleRadius + i * itemWidth, height / 2));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 先画中间的横线
        canvas.drawLine(points.get(0).x, (float) height / 2, points.get(points.size() - 1).x, (float) height / 2, mLinePaint);
        // 绘制刻度
        for (Point point : points) {
            canvas.drawLine(point.x, (float) height / 2 - lineHeight, point.x, (float) height / 2 + lineHeight, mLinePaint);
        }
        // 绘制文字
        for (int i = 0; i <= max; i++) {
            if (i == 0) {
                // 绘制第一个竖线下方的文字“小”，大小为11sp
                mTextPaint.setTextSize(sp2px(getContext(), 11));  // 设置字体大小
                canvas.drawText("小", points.get(i).x, (float) height / 2 + lineHeight + dp2px(getContext(), 20), mTextPaint);
            } else if (i == 2) {
                // 绘制第三个竖线下方的文字“标准”，大小为15sp
                mTextPaint.setTextSize(sp2px(getContext(), 15));
                canvas.drawText("标准", points.get(i).x, (float) height / 2 + lineHeight + dp2px(getContext(), 20), mTextPaint);
            } else if (i == 4) {
                // 绘制第五个竖线下方的文字“大”，大小为19sp
                mTextPaint.setTextSize(sp2px(getContext(), 19));
                canvas.drawText("大", points.get(i).x, (float) height / 2 + lineHeight + dp2px(getContext(), 20), mTextPaint);
            }
        }
        // 画圆
        float circleX;
        if (canMove) {
            // 随手指滑动过程
            if (currentX < circleRadius) {
                currentX = circleRadius;
            }
            if (currentX > width - circleRadius) {
                currentX = width - circleRadius;
            }
            circleX = currentX;
        } else {
            // 最终
            circleX = points.get(currentProgress).x;
        }
        // 实体圆
        canvas.drawCircle(circleX, circleY, circleRadius, mCirclePaint);
    }

    float downX = 0;
    private boolean canMove = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*过滤掉不合法的坐标
        if (event.getX() < circleRadius || event.getX() > width - circleRadius) {
            return false;
        }*/
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 判断是否是数据点
                downX = event.getX();
                canMove = isDownOnCircle(downX);
                break;
            case MotionEvent.ACTION_MOVE:
                if (canMove) {
                    currentX = event.getX();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                // 手指抬起之后就圆就不能在非有效点
                currentX = 0;
                float upX = event.getX();
                if (canMove) {
                    // 是滑动过来的，要判断距离哪个有效点最近，就滑动到哪个点
                    Point targetPoint = getNearestPoint(upX);
                    if (targetPoint != null) {
                        invalidate();
                    }
                } else {
                    if (Math.abs(downX - upX) < 30) {
                        Point point = isValidPoint(upX);
                        if (point != null) {
                            invalidate();
                        }
                    }
                }
                if (onPointResultListener != null) {
                    onPointResultListener.onPointResult(currentProgress);
                }
                downX = 0;
                canMove = false;
                break;
        }
        return true;
    }

    //滑动抬起之后，要滑动到最近的一个点那里
    private Point getNearestPoint(float x) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (Math.abs(point.x - x) < (float) itemWidth / 2) {
                currentProgress = i;
                return point;
            }
        }
        return null;
    }

    //判断是否点击到圆上
    private boolean isDownOnCircle(float x) {
        return Math.abs(points.get(currentProgress).x - x) < circleRadius;
    }

    //判断是否是有效的点击点
    private Point isValidPoint(float x) {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (Math.abs(point.x - x) < 30) {
                currentProgress = i;
                return point;
            }
        }
        return null;
    }

    public void setOnPointResultListener(OnPointResultListener onPointResultListener) {
        this.onPointResultListener = onPointResultListener;
    }

    private OnPointResultListener onPointResultListener;

    public void setCurrentProgress(int savedPosition) {
        this.currentProgress = savedPosition;
    }

    public void setLineColor(int color) {
        lineColor = color;
        mLinePaint.setColor(color);
        invalidate();  // 请求重绘
    }


    public interface OnPointResultListener {
        void onPointResult(int position);
    }

    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale + 0.5f);
    }

}