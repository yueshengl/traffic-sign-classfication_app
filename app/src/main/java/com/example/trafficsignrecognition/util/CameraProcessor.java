package com.example.trafficsignrecognition.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.trafficsignrecognition.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CameraProcessor {
    private CameraManager cameraManager;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;

    private final Context context;
    private final TextureView textureView;
    private final OverlayView overlayView;
    private final TextView tvOpenCamera;
    private final TextView tvSelectImage;
    private final TextView tvSelectVideo;
    private final ActivityResultLauncher<String> requestPermissionLauncher;
    private final TableLayout tableLayout;
    private final int savedPosition;
    private final TextToSpeech tts;
    private final RecognizeTool recognizeTool;


    private long lastProcessedTime = 0;  // 上次处理帧的时间戳
    private boolean isProcessingFrame = false;
    private static Boolean broadcast;

    public CameraProcessor(Context context, FrameLayout flTextureView, TextView tvOpenCamera, TextView tvSelectImage, TextView tvSelectVideo, ActivityResultLauncher<String> requestPermissionLauncher, TableLayout tableLayout,int savedPosition,TextToSpeech tts,RecognizeTool recognizeTool) {
        this.context = context;
        this.textureView = flTextureView.findViewById(R.id.texture_view);
        this.overlayView = flTextureView.findViewById(R.id.overlay_view);
        this.tvOpenCamera = tvOpenCamera;
        this.tvSelectImage = tvSelectImage;
        this.tvSelectVideo = tvSelectVideo;
        this.requestPermissionLauncher = requestPermissionLauncher;
        this.tableLayout = tableLayout;
        this.savedPosition = savedPosition;
        this.tts = tts;
        this.recognizeTool = recognizeTool;
    }

    public void openCamera() {
        // 获取 CameraManager
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            // 获取第一个后置摄像头
            cameraId = cameraManager.getCameraIdList()[0];
            // 检查是否有摄像头权限
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
                return;
            }
            // 打开摄像头
            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            Log.d("CameraProcessor", "Exception occurred: ", e);
        }
    }

    private void requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();  //创建预览
            tvOpenCamera.setText("相机已成功开启");
            tvSelectImage.setText("");
            tvSelectVideo.setText("");
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // 获取最佳预览尺寸
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);


            Size bestSize = getBestPreviewSize(outputSizes, textureView.getWidth(), textureView.getHeight());
            if (bestSize != null) {
                texture.setDefaultBufferSize(bestSize.getWidth(), bestSize.getHeight());
            }
            Surface surface = new Surface(texture);
            // 创建预览请求
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            // 创建 CameraCaptureSession
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    captureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    tvOpenCamera.setText("配置相机预览失败");
                    tvSelectImage.setText("");
                    tvSelectVideo.setText("");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.d("CameraProcessor", "Exception occurred: ", e);
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureSession.setRepeatingRequest(previewRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    long currentTime = System.currentTimeMillis();

                    if (currentTime - lastProcessedTime >= 500) {
                        lastProcessedTime = currentTime;  // 更新时间戳
                        processFrame();
                    }
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.d("CameraProcessor", "Exception occurred: ", e);
        }
    }

    private void processFrame() {
        if (isProcessingFrame) {
            return;  // 如果当前正在处理帧或者帧差异太小，则不进行新的处理
        }
        // 从 TextureView 获取当前帧的 Bitmap
        Bitmap bitmap = textureView.getBitmap();
        isProcessingFrame = true;
        if (bitmap != null) {
            new Thread(() -> {
                try {
                    // 执行识别逻辑
                    Pair<List<Integer>, List<float[]>> result = recognizeTool.recognizeFrame(bitmap);
                    List<float[]> detectedBoxes = result.second;
                    List<String> recognizeResult = new ArrayList<>();
                    for (Integer i: result.first) {
                        recognizeResult.add(ConstantUtil.name[i]);
                    }
                    // 将检测到的框转换为 Rect，并在主线程更新 UI
                    List<Rect> rect = new ArrayList<>();
                    List<int[]> position = new ArrayList<>();
                    for (int i = 0;i< detectedBoxes.size();i++) {
                        float[] box =detectedBoxes.get(i);
                        int x1 = (int) ((box[0] - box[2] / 2) * textureView.getWidth());
                        int y1 = (int) ((box[1] - box[2] / 2) * textureView.getHeight());
                        int x2 = (int) ((box[0] + box[2] / 2) * textureView.getWidth());
                        int y2 = (int) ((box[1] + box[2] / 2) * textureView.getHeight());

                        rect.add(new Rect(x1, y1, x2, y2));
                        position.add(new int[]{x1, y1, y2});
                    }
                    // 在主线程中更新 OverlayView以及tableLayout
                    ((Activity) context).runOnUiThread(() -> {
                        overlayView.setDetectedRect(rect,position);
                        recognizeTool.removeTableRow(tableLayout);
                        recognizeTool.updateTableLayout(context, tableLayout, recognizeResult, savedPosition);
                    });
                    Log.d("TTS","broadcast:"+ broadcast);
                    if(Boolean.TRUE.equals(broadcast)){
                        //将result.first语音播报出来
                        tts.predict(result.first);
                    }
                } catch (IOException e) {
                    Log.d("CameraProcessor", "Exception occurred: ", e);
                } finally {
                    isProcessingFrame = false;  // 处理完毕，允许下一次处理
                }
            }).start();
        } else {
           isProcessingFrame = false;  // 如果获取 Bitmap 失败，允许下一次处理
        }
    }


    public void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    public static void updateBroadcast(Boolean isBroadcasting) {
      broadcast = isBroadcasting;
    }

    private static Size getBestPreviewSize(Size[] sizes, int width, int height) {
        // 计算目标宽高比
        double targetRatio = (double) width / height;
        Size bestSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();

            // 检查宽高比是否匹配
            if (Math.abs(ratio - targetRatio) > 0.1) continue;

            // 选择最接近的尺寸
            if (Math.abs(size.getHeight() - height) < minDiff) {
                bestSize = size;
                minDiff = Math.abs(size.getHeight() - height);
            }
        }
        // 如果没有找到合适的宽高比，选择最大的尺寸
        if (bestSize == null) {
            for (Size size : sizes) {
                if (size.getHeight() <= height && size.getWidth() <= width) {
                    if (bestSize == null || size.getHeight() > bestSize.getHeight()) {
                        bestSize = size;
                    }
                }
            }
        }
        return bestSize;
    }
}
