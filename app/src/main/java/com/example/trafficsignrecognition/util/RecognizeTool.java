package com.example.trafficsignrecognition.util;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getCommonFontSize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.trafficsignrecognition.R;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class RecognizeTool {

    private final Interpreter interpreter1;
    private final Interpreter interpreter2;
    private Context context;

    public RecognizeTool(Context context) {
        interpreter1 = DetectTool.getInterpreter(context);
        interpreter2 = getInterpreter(context);
    }

    public Interpreter getInterpreter1() {
        return interpreter1;
    }

    public Interpreter getInterpreter2() {
        return interpreter2;
    }

    // 从Assets下加载lite文件
    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        this.context = context;
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(ConstantUtil.RECOGNIZE_MODEL);
        FileChannel fileChannel;
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // 构建Interpreter，这是lite文件的解释器
    public Interpreter getInterpreter(Context context) {
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

    //定义argmax函数
    public int argmax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    //识别图片
    public Pair<List<String>, Bitmap> recognizeImage(Bitmap bitmap) {
        List<String> recognizeResults = new ArrayList<>();
        // 将要处理的Bitmap图像缩放为640×640
        Bitmap resize_bitmap = DetectTool.resizeBitmap(bitmap, 640);
        // 转换为输入层(1, 640, 640, 3)结构的float数组
        float[][][][] input_arr = DetectTool.bitmapToFloatArray(resize_bitmap);
        // 构建一个空的输出结构
        float[][][] outArray = new float[1][5][8400];
        // 运行解释器，input_arr是输入，它会将结果写到outArray中
        interpreter1.run(input_arr, outArray);

        // 取出(1, 5, 8400)中的(5, 8400)
        float[][] matrix_2d = outArray[0];
        // (5, 8400)变为(8400, 5)
        float[][] outputMatrix = new float[8400][5];
        for (int i = 0; i < 8400; i++) {
            for (int j = 0; j < 5; j++) {
                outputMatrix[i][j] = matrix_2d[j][i];
            }
        }
        float threshold = 0.08f; // 类别准确率筛选
        float non_max = 0.9f; // nms非极大值抑制
        ArrayList<float[]> boxes = new ArrayList<>();
        ArrayList<Float> scores = new ArrayList<>();
        for (float[] detection : outputMatrix) {
            // 5位数中的最后一位是置信度
            float score = detection[4];
            if (score >= threshold) { // 如果置信度超过60%则记录
                boxes.add(detection); // 筛选后的框
                scores.add(score); // 筛选后的准确率
            }
        }
        List<float[]> results = DetectTool.nonMaxSuppression(boxes, scores, non_max);
        // 创建可变的位图用于绘制
        Bitmap mutableImage = resize_bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableImage);
        // 用于绘制方框的Paint
        Paint paint = new Paint();
        paint.setColor(0xFFFF0000); // 红色
        paint.setStrokeWidth(1); // 边框宽度
        paint.setStyle(Paint.Style.STROKE);
        // 用于绘制文本的Paint
        Paint textPaint = new Paint();
        textPaint.setColor(0xFFFF0000);
        textPaint.setTextSize(15); // 设置字体大小
        textPaint.setStyle(Paint.Style.FILL);
        int width = mutableImage.getWidth();
        int height = mutableImage.getHeight();
        for (int i = 0; i < results.size(); i++) {
            float x = results.get(i)[0];
            float y = results.get(i)[1];
            float w = results.get(i)[2];
            float h = results.get(i)[3];

            int x1 = (int) ((x - w / 2) * width);
            int y1 = (int) ((y - h / 2) * height);
            int x2 = (int) ((x + w / 2) * width);
            int y2 = (int) ((y + h / 2) * height);
            // 绘制矩形
            Rect rect = new Rect(x1, y1, x2, y2);
            canvas.drawRect(rect, paint);
            // 获取要绘制的标签文本
            String label = String.valueOf(i + 1);
            // 计算文本的宽度
            float textWidth = textPaint.measureText(label);
            // 在矩形左方绘制标签
            // 注意：这里我们使用了(y1 + y2) / 2来计算文本垂直居中的位置
            // 如果需要对齐顶部或底部，可以相应地调整这个值
            canvas.drawText(label, x1 - textWidth - 5, (float) (y1 + y2) / 2 + textPaint.getTextSize() / 2, textPaint);
            // 确保裁剪区域在有效范围内
            // 扩大边界框：向外扩展2个像素
            int offset = 2;
            x1 = Math.max(x1 - offset, 0); // 确保 x1 不小于 0
            y1 = Math.max(y1 - offset, 0); // 确保 y1 不小于 0
            x2 = Math.min(x2 + offset, resize_bitmap.getWidth() - 1); // 确保 x2 不大于图像宽度
            y2 = Math.min(y2 + offset, resize_bitmap.getHeight() - 1); // 确保 y2 不大于图像高度
            // 计算裁剪区域的宽度和高度
            int cropWidth = x2 - x1 + 1;
            int cropHeight = y2 - y1 + 1;

            // 裁剪图像
            Bitmap croppedBitmap = Bitmap.createBitmap(resize_bitmap, x1, y1, cropWidth, cropHeight);

            // 转换为输入层(1, 32, 32, 3)结构的float数组
            float[][][][] input = DetectTool.bitmapToFloatArray(DetectTool.resizeBitmap(croppedBitmap, 32));
            // 构建一个空的输出结构
            float[][] output = new float[1][43];
            // 运行解释器，input是输入，它会将结果写到output中
            interpreter2.run(input, output);
            // 获取预测类别
            int predictedLabel = argmax(output[0]);
            // 保存识别结果
            recognizeResults.add(ConstantUtil.name[predictedLabel]);
        }
        return new Pair<>(recognizeResults, mutableImage);
    }

    public Pair<List<Integer>, List<float[]>> recognizeFrame(Bitmap bitmap) throws IOException {
        List<Integer> recognizeResults = new ArrayList<>();
        List<float[]> detectedBoxes = new ArrayList<>();
        // 将要处理的Bitmap图像缩放为640×640
        Bitmap resize_bitmap = DetectTool.resizeBitmap(bitmap, 640);
        // 转换为输入层(1, 640, 640, 3)结构的float数组
        float[][][][] input_arr = DetectTool.bitmapToFloatArray(resize_bitmap);
        // 构建一个空的输出结构
        float[][][] outArray = new float[1][5][8400];
        // 运行解释器，input_arr是输入，它会将结果写到outArray中
        interpreter1.run(input_arr, outArray);

        // 取出(1, 5, 8400)中的(5, 8400)
        float[][] matrix_2d = outArray[0];
        // (5, 8400)变为(8400, 5)
        float[][] outputMatrix = new float[8400][5];
        for (int i = 0; i < 8400; i++) {
            for (int j = 0; j < 5; j++) {
                outputMatrix[i][j] = matrix_2d[j][i];
            }
        }
        float threshold = 0.08f; // 类别准确率筛选
        float non_max = 0.9f; // nms非极大值抑制
        ArrayList<float[]> boxes = new ArrayList<>();
        ArrayList<Float> scores = new ArrayList<>();
        for (float[] detection : outputMatrix) {
            // 5位数中的最后一位是置信度
            float score = detection[4];
            if (score >= threshold) { // 如果置信度超过60%则记录
                boxes.add(detection); // 筛选后的框
                scores.add(score); // 筛选后的准确率
            }
        }
        List<float[]> results = DetectTool.nonMaxSuppression(boxes, scores, non_max);

        int width = resize_bitmap.getWidth();
        int height = resize_bitmap.getHeight();
        for (int i = 0; i < results.size(); i++) {
            float x = results.get(i)[0];
            float y = results.get(i)[1];
            float w = results.get(i)[2];
            float h = results.get(i)[3];

            detectedBoxes.add(new float[]{x, y, w, h});

            int x1 = (int) ((x - w / 2) * width);
            int y1 = (int) ((y - h / 2) * height);
            int x2 = (int) ((x + w / 2) * width);
            int y2 = (int) ((y + h / 2) * height);

            // 确保裁剪区域在有效范围内
            // 扩大边界框：向外扩展2个像素
            int offset = 2;
            x1 = Math.max(x1 - offset, 0); // 确保 x1 不小于 0
            y1 = Math.max(y1 - offset, 0); // 确保 y1 不小于 0
            x2 = Math.min(x2 + offset, resize_bitmap.getWidth() - 1); // 确保 x2 不大于图像宽度
            y2 = Math.min(y2 + offset, resize_bitmap.getHeight() - 1); // 确保 y2 不大于图像高度
            // 计算裁剪区域的宽度和高度
            int cropWidth = x2 - x1 + 1;
            int cropHeight = y2 - y1 + 1;

            // 裁剪图像
            Bitmap croppedBitmap = Bitmap.createBitmap(resize_bitmap, x1, y1, cropWidth, cropHeight);
            // 转换为输入层(1, 32, 32, 3)结构的float数组
            float[][][][] input = DetectTool.bitmapToFloatArray(DetectTool.resizeBitmap(croppedBitmap, 32));
            // 构建一个空的输出结构
            float[][] output = new float[1][43];
            // 运行解释器，input是输入，它会将结果写到output中
            interpreter2.run(input, output);
            // 获取预测类别
            int predictedLabel = argmax(output[0]);
            // 保存识别结果
            recognizeResults.add(predictedLabel);
        }
        return new Pair<>(recognizeResults, detectedBoxes);
    }


    @SuppressLint("ResourceAsColor")
    public void updateTableLayout(Context context,TableLayout tableLayout, List<String> recognizeResult, int savedPosition) {
        for (int i = 0; i < recognizeResult.size(); i++) {
            // 创建一个新的TableRow
            TableRow tableRow = new TableRow(tableLayout.getContext());
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            tableRow.setBackgroundResource(R.drawable.table_cell_border);
            tableRow.setPaddingRelative((int) context.getResources().getDimension(R.dimen.padding_start),
                    0, (int) context.getResources().getDimension(R.dimen.padding_end), 0);

            // 创建序号列
            TextView serialTextView = new TextView(tableLayout.getContext());
            serialTextView.setLayoutParams(new TableRow.LayoutParams(
                    0, // 宽度设置为0，因为我们将使用layout_weight
                    TableRow.LayoutParams.WRAP_CONTENT, // 高度设置为wrap_content
                    1.0f)); // 设置权重
            serialTextView.setGravity(Gravity.CENTER);
            serialTextView.setText(String.valueOf(i + 1));
            serialTextView.setTextColor(context.getResources().getColor(R.color.black));
            serialTextView.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));

            // 创建分隔线View
            View divider = new View(tableLayout.getContext());
            divider.setLayoutParams(new TableRow.LayoutParams(
                    (int) context.getResources().getDimension(R.dimen.divider_width), // 分隔线宽度
                    TableRow.LayoutParams.MATCH_PARENT)); // 高度设置为match_parent
            divider.setBackgroundResource(R.drawable.divider);

            // 创建识别结果列
            TextView resultTextView = new TextView(tableLayout.getContext());
            resultTextView.setLayoutParams(new TableRow.LayoutParams(
                    0, // 宽度设置为0，因为我们将使用layout_weight
                    TableRow.LayoutParams.WRAP_CONTENT, // 高度设置为wrap_content
                    1.0f)); // 设置权重
            resultTextView.setGravity(Gravity.CENTER);
            resultTextView.setText(recognizeResult.get(i));
            resultTextView.setTextColor(context.getResources().getColor(R.color.black));
            resultTextView.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));

            // 将所有视图添加到TableRow
            tableRow.addView(serialTextView);
            tableRow.addView(divider);
            tableRow.addView(resultTextView);

            // 将新行添加到TableLayout中
            tableLayout.addView(tableRow);
        }
    }

    public void removeTableRow(TableLayout tableLayout) {
        // 获取TableLayout中的子视图数量
        int childCount = tableLayout.getChildCount();
        // 从第二个子视图开始遍历，因为第一个子视图是表头
        for (int i = childCount - 1; i > 0; i--) { // 从后向前遍历，避免在遍历时修改集合导致的问题
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                // 移除当前的TableRow
                tableLayout.removeViewAt(i);
            }
        }
    }
}
