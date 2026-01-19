package com.example.trafficsignrecognition.util;

import static com.example.trafficsignrecognition.util.FilePathUtil.getRealPathFromUri;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoProcessor {
    private static final int RECOGNIZE_SUCCESS = 1;
    private static final int RECOGNIZE_FAILED = 0;
    private final Context context;
    private final VideoView vvContent;
    private final int savedPosition;
    private final TableLayout tableLayout;
    private final ProgressDialog progressDialog;
    private final RecognizeTool recognizeTool;
    private File outputDir;
    private List<Bitmap> frameBitmap;
    private List<Bitmap> resizedBitmap;
    // 添加一个标志位来控制线程的停止
    private boolean isProcessingFrames = true;

    public VideoProcessor(Context context, VideoView vvContent, int savedPosition, TableLayout tableLayout,ProgressDialog progressDialog,RecognizeTool recognizeTool) {
        this.context = context;
        this.vvContent = vvContent;
        this.savedPosition = savedPosition;
        this.tableLayout = tableLayout;
        this.progressDialog = progressDialog;
        this.recognizeTool = recognizeTool;
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RECOGNIZE_SUCCESS) { // 成功识别
                List<String> result = (List<String>) msg.obj;
                if (result != null&& !result.isEmpty()) {

                    // 创建 MediaPlayer
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnPreparedListener(mp -> mp.setLooping(true));  // 设置循环播放

                    // 处理帧
                    List<Bitmap> annotatedFrames = new ArrayList<>();
                    for (int i = 0; i < frameBitmap.size(); i++) {
                        // 还原帧到原始尺寸
                        Bitmap annotatedFrame = DetectTool.restoreOriginalShape(resizedBitmap.get(i), frameBitmap.get(i).getWidth(), frameBitmap.get(i).getHeight());
                        annotatedFrames.add(annotatedFrame);
                    }
                    // 开始播放帧
                    isProcessingFrames = true;
                    playFramesOnVideoView(vvContent, annotatedFrames);
                    // 更新表格显示识别结果
                    recognizeTool.removeTableRow(tableLayout);
                    recognizeTool.updateTableLayout(context, tableLayout, result, savedPosition);
                }else {
                    Toast.makeText(context, "未识别到交通标志！", Toast.LENGTH_LONG).show();
                }
            } else if (msg.what == RECOGNIZE_FAILED) { // 异常处理
                Exception e = (Exception) msg.obj;
                Log.d("VideoProcessor", "Error processing video", e);
                Toast.makeText(context, "识别失败，请重试！", Toast.LENGTH_LONG).show();
            }
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    };

    private void playFramesOnVideoView(VideoView videoView, List<Bitmap> frames) {
        Handler frameHandler = new Handler(Looper.getMainLooper());
        final int frameDuration = 1000;  // 每帧持续1秒
        final int[] currentFrame = {0};  // 使用数组来保持可变性

        frameHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isProcessingFrames && currentFrame[0] < frames.size()) {
                    videoView.setBackground(new BitmapDrawable(context.getResources(), frames.get(currentFrame[0])));
                    currentFrame[0]++;
                    frameHandler.postDelayed(this, frameDuration);  // 显示下一帧
                } else if (!isProcessingFrames) {
                    // 停止帧处理
                    frameHandler.removeCallbacks(this);
                } else {
                    currentFrame[0] = 0;  // 重置为第一帧，循环播放
                    frameHandler.postDelayed(this, frameDuration);
                }
            }
        });
    }

    public void processVideo(Uri videoUri) {
        // 显示 ProgressDialog
        progressDialog.setMessage("正在识别中...");
        progressDialog.setCancelable(false);  // 禁止手动取消
        progressDialog.show();

        new Thread(() -> {
            try {
                // 获取视频路径
                String videoFilePath = getRealPathFromUri(context, videoUri, "video");

                // 提取帧的输出目录
                outputDir = new File(context.getExternalFilesDir(null), "extracted_frames_" + System.currentTimeMillis());
                if (!outputDir.exists()) {
                    if (outputDir.mkdirs()) {
                        Log.e("VideoProcessor", "创建文件夹成功");
                    } else {
                        Log.e("VideoProcessor", "创建文件夹失败");
                    }
                }

                // 使用 FFmpeg 提取每秒一帧
                String framesOutputPattern = outputDir.getAbsolutePath() + "/frame_%04d.png";
                String extractFramesCmd = "-i " + videoFilePath + " -vf fps=1 " + framesOutputPattern;

                int extractResult = FFmpeg.execute(extractFramesCmd);

                if (extractResult == Config.RETURN_CODE_SUCCESS) {
                    Log.d("FFmpeg", "Frames extracted successfully.");
                    // 获取提取的所有帧
                    File[] frames = outputDir.listFiles();
                    // 去除重复帧
                    assert frames != null;
                    List<File> uniqueFrames = FrameDifferenceUtil.removeDuplicateFrames(Arrays.asList(frames), 5.0); // 根据需要调整阈值
                    // 记录交通标志数量
                    int signNum = 0;
                    List<String> recognizeResults = new ArrayList<>();
                    frameBitmap = new ArrayList<>();
                    resizedBitmap = new ArrayList<>();
                    if (!uniqueFrames.isEmpty()) {
                        for (File frameFile : uniqueFrames) {
                            Bitmap frame_bitmap = BitmapFactory.decodeFile(frameFile.getAbsolutePath());
                            frameBitmap.add(frame_bitmap);
                            // 将要处理的Bitmap图像缩放为640×640
                            Bitmap resize_bitmap = DetectTool.resizeBitmap(frame_bitmap, 640);
                            // 转换为输入层(1, 640, 640, 3)结构的float数组
                            float[][][][] input_arr = DetectTool.bitmapToFloatArray(resize_bitmap);
                            // 构建一个空的输出结构
                            float[][][] outArray = new float[1][5][8400];
                            // 运行解释器，input_arr是输入，它会将结果写到outArray中
                            recognizeTool.getInterpreter1().run(input_arr, outArray);

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
                                String label = String.valueOf(++signNum);
                                // 计算文本的宽度
                                float textWidth = textPaint.measureText(label);
                                // 在矩形左方绘制标签
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
                                recognizeTool.getInterpreter2().run(input, output);
                                // 获取预测类别
                                int predictedLabel = recognizeTool.argmax(output[0]);
                                // 保存识别结果和修改后的图片
                                recognizeResults.add(ConstantUtil.name[predictedLabel]);
                            }
                            resizedBitmap.add(mutableImage);
                        }
                    }
                    // 发送成功结果
                    Message message = handler.obtainMessage(RECOGNIZE_SUCCESS, recognizeResults);
                    handler.sendMessage(message);
                } else {
                    Log.e("FFmpeg", "Frame extraction failed.");
                }
            } catch (Exception e) {
                // 发送异常信息
                Message message = handler.obtainMessage(RECOGNIZE_FAILED, e);
                handler.sendMessage(message);
            }
        }).start();
    }
    // 当需要停止帧处理时调用此方法
    public void stopFrameProcessing() {
        isProcessingFrames = false;
    }
}
