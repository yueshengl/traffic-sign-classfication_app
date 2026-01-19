package com.example.trafficsignrecognition.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FrameDifferenceUtil {

     //计算两张 Bitmap 图片之间的平均像素差异
    public static double computeFrameDifference(Bitmap img1, Bitmap img2) {
        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());
        long diff = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = img1.getPixel(x, y);
                int pixel2 = img2.getPixel(x, y);

                int r1 = (pixel1 >> 16) & 0xff;
                int g1 = (pixel1 >> 8) & 0xff;
                int b1 = pixel1 & 0xff;

                int r2 = (pixel2 >> 16) & 0xff;
                int g2 = (pixel2 >> 8) & 0xff;
                int b2 = pixel2 & 0xff;

                diff += Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);
            }
        }
        // 计算平均像素差异
        return diff / (width * height * 3.0);
    }



    public static List<File> removeDuplicateFrames(List<File> frames, double threshold) {
        List<File> uniqueFrames = new ArrayList<>();
        Bitmap prevFrame = null;
        for (File frameFile : frames) {
            Bitmap currentFrame = BitmapFactory.decodeFile(frameFile.getAbsolutePath());
            if (prevFrame == null) {
                uniqueFrames.add(frameFile);
                prevFrame = currentFrame;
            } else {
                double diff = computeFrameDifference(prevFrame, currentFrame);
                if (diff > threshold) {
                    uniqueFrames.add(frameFile);
                    prevFrame = currentFrame;
                } else {
                    boolean isDeleted = frameFile.delete(); // 删除重复帧
                    if (!isDeleted) {
                        System.err.println("Failed to delete file: " + frameFile.getAbsolutePath());
                    }
                }
            }
        }
        return uniqueFrames;
    }
}


