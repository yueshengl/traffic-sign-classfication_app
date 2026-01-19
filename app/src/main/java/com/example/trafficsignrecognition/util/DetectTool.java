package com.example.trafficsignrecognition.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetectTool {

    // 从Assets下加载lite文件
    private static MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(ConstantUtil.DETECT_MODEL);
        FileChannel fileChannel;
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // 构建Interpreter，这是lite文件的解释器
    public static Interpreter getInterpreter(Context context) {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        Interpreter interpreter;
        try {
            interpreter = new Interpreter(loadModelFile(context), options);
        } catch (IOException e) {
            throw new RuntimeException("Error loading model file.", e);
        }
        return interpreter;
    }

    public static Bitmap resizeBitmap(Bitmap source, int maxSize) {
        int outWidth;
        int outHeight;
        int inWidth = source.getWidth();
        int inHeight = source.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(source, outWidth, outHeight, false);
        Bitmap outputImage = Bitmap.createBitmap(maxSize, maxSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputImage);
        canvas.drawColor(Color.WHITE);
        int left = (maxSize - outWidth) / 2;
        int top = (maxSize - outHeight) / 2;
        canvas.drawBitmap(resizedBitmap, left, top, null);
        return outputImage;
    }

    public static float[][][][] bitmapToFloatArray(Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        // 初始化一个float数组
        float[][][][] result = new float[1][height][width][3];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                // 获取像素值
                int pixel = bitmap.getPixel(j, i);
                // 将RGB值分离并进行标准化（假设你需要将颜色值标准化到0-1之间）
                result[0][i][j][0] = ((pixel >> 16) & 0xFF) / 255.0f;
                result[0][i][j][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                result[0][i][j][2] = (pixel & 0xFF) / 255.0f;
            }
        }
        return result;
    }

    public static float iou(float[] box1, float[] box2) {
        float x1 = Math.max(box1[0], box2[0]);
        float y1 = Math.max(box1[1], box2[1]);
        float x2 = Math.min(box1[2], box2[2]);
        float y2 = Math.min(box1[3], box2[3]);
        float interArea = Math.max(0, x2 - x1 + 1) * Math.max(0, y2 - y1 + 1);
        float box1Area = (box1[2] - box1[0] + 1) * (box1[3] - box1[1] + 1);
        float box2Area = (box2[2] - box2[0] + 1) * (box2[3] - box2[1] + 1);
        return interArea / (box1Area + box2Area - interArea);
    }

    public static List<float[]> nonMaxSuppression(List<float[]> boxes, List<Float> scores, float threshold) {
        List<float[]> result = new ArrayList<>();
        while (!boxes.isEmpty()) {
            int bestScoreIdx = scores.indexOf(Collections.max(scores));
            float[] bestBox = boxes.get(bestScoreIdx);
            result.add(bestBox);
            boxes.remove(bestScoreIdx);
            scores.remove(bestScoreIdx);
            List<float[]> newBoxes = new ArrayList<>();
            List<Float> newScores = new ArrayList<>();
            for (int i = 0; i < boxes.size(); i++) {
                if (iou(bestBox, boxes.get(i)) < threshold) {
                    newBoxes.add(boxes.get(i));
                    newScores.add(scores.get(i));
                }
            }
            boxes = newBoxes;
            scores = newScores;
        }
        return result;
    }

    public static Bitmap restoreOriginalShape(Bitmap resizeBitmap, int originalWidth, int originalHeight) {

        // 移除白色填充并裁剪有效图像
        Rect srcRect = calculateNonPaddingRect(resizeBitmap);
        Bitmap croppedBitmap = Bitmap.createBitmap(resizeBitmap, srcRect.left, srcRect.top, srcRect.width(), srcRect.height());

        return  Bitmap.createScaledBitmap(croppedBitmap, originalWidth, originalHeight, true);
    }

    private static Rect calculateNonPaddingRect(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int left = width;
        int top = height;
        int right = 0;
        int bottom = 0;

        // 找到非白色区域的边界
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitmap.getPixel(x, y) != Color.WHITE) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }

        // 返回包围非白色区域的矩形
        return new Rect(left, top, right, bottom);
    }

}