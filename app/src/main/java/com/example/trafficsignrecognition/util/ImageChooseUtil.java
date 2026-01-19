package com.example.trafficsignrecognition.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.trafficsignrecognition.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageChooseUtil {


    // 定义请求码
    private static final int REQUEST_CODE_TAKE_PHOTO = 1;
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 2;
    private static final int REQUEST_CODE_CROP_IMAGE = 3;
    private static Uri photoURI;//拍照后存的图片的URI

    // 显示选择拍照还是从相册中选择照片的对话框
    @SuppressLint({"QueryPermissionsNeeded", "IntentReset"})
    public static void showPhotoOptionsDialog(Activity activity) {
        //检查权限
        //Permission.checkPermission(this);
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("选择图片来源");

        // 添加选项
        String[] options = {"从相册选择", "使用相机拍摄"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // 从相册选择
                    Intent intentChoose = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intentChoose.setType("image/*");
                    activity.startActivityForResult(intentChoose, REQUEST_CODE_CHOOSE_PHOTO);
                    break;
                case 1:
                    // 调用系统相机拍照
                    Log.e("dai", "调用系统相机拍照");
                    Permission.checkCameraPermission(activity);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        // 创建临时文件，用于保存拍照后的图片
                        File photoFile = null;
                        try {
                            photoFile = createImageFile(activity);
                        } catch (IOException ex) {
                            Log.d("ImageChooseUtil", "Exception occurred: ", ex);
                        }
                        if (photoFile != null) {
                            // 通过FileProvider将File转化为Uri，否则在7.0以上的系统上会出现FileUriExposedException
                            //拍照后存的图片的URI
                            photoURI = FileProvider.getUriForFile(activity, "com.example.trafficsignrecognition.fileProvider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            activity.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
                        }
                    }
                    break;
            }
        });

        // 显示对话框
        builder.create().show();
    }


    //生成临时文件
    public static File createImageFile(Activity activity) throws IOException {
        // 创建图片文件名
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* 前缀 */
                ".jpg",         /* 后缀 */
                storageDir      /* 目录 */
        );
    }


    /**
     * 裁剪图片
     *
     * @param uri 待裁剪图片的Uri
     */
    public static void cropImage(Uri uri, Activity activity) {
        Uri destinationUri = Uri.fromFile(new File(activity.getExternalFilesDir(null), System.currentTimeMillis() + ".jpg"));
        UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(500, 500)
                .withOptions(getCropOptions(activity))
                .start(activity, REQUEST_CODE_CROP_IMAGE);
    }


    //自定义裁剪界面样式
    public static UCrop.Options getCropOptions(Activity activity) {
        UCrop.Options options = new UCrop.Options();
        //设置裁剪框为圆形
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setShowCropGrid(false);
        //设置裁剪框的颜色和透明度
        options.setDimmedLayerColor(ContextCompat.getColor(activity, R.color.black));
        //设置裁剪框的线框
        options.setToolbarColor(ContextCompat.getColor(activity, R.color.grey));
        options.setStatusBarColor(ContextCompat.getColor(activity, R.color.grey));
        options.setToolbarWidgetColor(ContextCompat.getColor(activity, R.color.white));
        options.setToolbarTitle("裁剪图片");
        return options;
    }


    public static Uri getPhotoURI() {
        return photoURI;
    }


}
