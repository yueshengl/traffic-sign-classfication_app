package com.example.trafficsignrecognition.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.List;

public class ImageProcessor {
    private static final int RECOGNIZE_SUCCESS = 1;
    private static final int RECOGNIZE_FAILED = 0;
    private final Context context;
    private final ImageView ivContent;
    private final int savedPosition;
    private final TableLayout tableLayout;
    private final ProgressDialog progressDialog;
    private final RecognizeTool recognizeTool;

    public ImageProcessor(Context context, ImageView ivContent, int savedPosition, TableLayout tableLayout,ProgressDialog progressDialog,RecognizeTool recognizeTool) {
        this.context = context;
        this.ivContent = ivContent;
        this.savedPosition = savedPosition;
        this.tableLayout = tableLayout;
        this.progressDialog = progressDialog;
        this.recognizeTool = recognizeTool;
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RECOGNIZE_SUCCESS) { // 成功识别
                Pair<List<String>, Bitmap> result = (Pair<List<String>, Bitmap>) msg.obj;
                Bitmap bitmap = msg.getData().getParcelable("bitmap");
                if (result.first != null&&result.first.size()!=0) {
                    assert bitmap != null;
                    ivContent.setImageBitmap(DetectTool.restoreOriginalShape(result.second, bitmap.getWidth(), bitmap.getHeight()));
                    recognizeTool.removeTableRow(tableLayout);
                    recognizeTool.updateTableLayout(context, tableLayout, result.first, savedPosition);
                }else {
                    Toast.makeText(context, "未识别到交通标志！", Toast.LENGTH_LONG).show();
                }
            } else if (msg.what == RECOGNIZE_FAILED) { // 异常处理
                Exception e = (Exception) msg.obj;
                Log.d("ImageProcessor", "Exception occurred: ", e);
                Toast.makeText(context, "识别失败，请重试！", Toast.LENGTH_LONG).show();
            }
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    };

    public void processImage(Uri imageUri) {
        // 显示 ProgressDialog
        progressDialog.setMessage("正在识别中...");
        progressDialog.setCancelable(false);  // 禁止手动取消
        progressDialog.show();

        new Thread(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                Pair<List<String>, Bitmap> result = recognizeTool.recognizeImage(bitmap);

                // 发送成功结果
                Message message = handler.obtainMessage(RECOGNIZE_SUCCESS, result);
                Bundle bundle = new Bundle();
                bundle.putParcelable("bitmap", bitmap);
                message.setData(bundle);
                handler.sendMessage(message);
            } catch (Exception e) {
                // 发送异常信息
                Message message = handler.obtainMessage(RECOGNIZE_FAILED, e);
                handler.sendMessage(message);
            }
        }).start();
    }
}
