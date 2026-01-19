package com.example.trafficsignrecognition.ui.home;

import static android.app.Activity.RESULT_OK;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.example.trafficsignrecognition.util.CameraProcessor.updateBroadcast;
import static com.example.trafficsignrecognition.util.FilePathUtil.getRealPathFromUri;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getAlertSize;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getCommonFontSize;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.trafficsignrecognition.MyApplication;
import com.example.trafficsignrecognition.R;
import com.example.trafficsignrecognition.activity.MainActivity;
import com.example.trafficsignrecognition.databinding.FragmentHomeBinding;
import com.example.trafficsignrecognition.util.CameraProcessor;
import com.example.trafficsignrecognition.util.ImageProcessor;
import com.example.trafficsignrecognition.util.RecognizeTool;
import com.example.trafficsignrecognition.util.SharedViewModel;
import com.example.trafficsignrecognition.util.TextToSpeech;
import com.example.trafficsignrecognition.util.VideoProcessor;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ImageView ivContent;
    private VideoView vvContent;

    // 定义请求码
    private static final int REQUEST_SELECT_IMAGE = 101;
    private static final int REQUEST_SELECT_VIDEO = 102;
    private TextView tvSelectImage;
    private TextView tvSelectVideo;
    private TextView tvOpenCamera;
    private TextView tvBackground;



    private Button btnSelectImage;
    private Button btnSelectVideo;
    private Button btnOpenCamera;
    private TableLayout tableLayout;
    private int savedPosition;
    private SharedViewModel viewModel;
    private FrameLayout flTextureView;
    private TableLayout tableLayout2;
    private CameraProcessor cameraProcessor;
    private RecognizeTool recognizeToolForImage;
    private RecognizeTool recognizeToolForVideo;
    private RecognizeTool recognizeToolForCamera;
    private TextToSpeech tts;
    private VideoProcessor videoProcessor;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        //先初始化模型以节省时间
        recognizeToolForImage = new RecognizeTool(getContext());
        recognizeToolForVideo = new RecognizeTool(getContext());
        recognizeToolForCamera = new RecognizeTool(getContext());

        tvBackground = binding.tvBackground;
        ivContent = binding.ivContent;
        vvContent = binding.vvContent;
        flTextureView = binding.flTextureView;
        btnSelectImage = binding.btnSelectImage;
        btnSelectVideo = binding.btnSelectVideo;
        btnOpenCamera = binding.btnOpenCamera;
        tvSelectImage = binding.tvSelectImage;
        tvSelectVideo = binding.tvSelectVideo;
        tvOpenCamera = binding.tvOpenCamera;
        tableLayout = binding.tableLayout;
        tableLayout2 = binding.tableLayout2;

        tvSelectImage.setMovementMethod(new ScrollingMovementMethod());
        tvSelectVideo.setMovementMethod(new ScrollingMovementMethod());
        tvOpenCamera.setMovementMethod(new ScrollingMovementMethod());

        handleTextSize();

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_SELECT_IMAGE);
        });
        btnSelectVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            startActivityForResult(intent, REQUEST_SELECT_VIDEO);
        });
        //是否进行语音播报
        SharedPreferences preferences = Objects.requireNonNull(getContext()).getSharedPreferences("Switch", Context.MODE_PRIVATE);
        Boolean switchBroadcast = preferences.getBoolean("broadcast",  true);  // 如果没有存储值，默认为true
        viewModel.setBroadcast(switchBroadcast);
        btnOpenCamera.setOnClickListener(v -> {
            tvBackground.setVisibility(View.GONE);
            ivContent.setVisibility(View.GONE);
            vvContent.setVisibility(View.GONE);
            flTextureView.setVisibility(View.VISIBLE);
            recognizeToolForCamera.removeTableRow(tableLayout2);
            tableLayout.setVisibility(View.GONE);
            tableLayout2.setVisibility(View.VISIBLE);
            //启动摄像机
            cameraProcessor = new CameraProcessor(getContext(), flTextureView, tvOpenCamera,tvSelectImage,tvSelectVideo,requestPermissionLauncher,tableLayout2,savedPosition,tts,recognizeToolForCamera);
            updateBroadcast(viewModel.getBroadcast().getValue());
            cameraProcessor.openCamera();
        });


        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        viewModel.setThemeColor(themeColor);
        viewModel.getThemeColor().observe(getViewLifecycleOwner(), newColor -> {
            int color = ContextCompat.getColor(getContext(), newColor);  // 替换为你想要的颜色
            ViewCompat.setBackgroundTintList(btnSelectImage, ColorStateList.valueOf(color));
            ViewCompat.setBackgroundTintList(btnSelectVideo, ColorStateList.valueOf(color));
            ViewCompat.setBackgroundTintList(btnOpenCamera, ColorStateList.valueOf(color));
        });

        //获取TTS对象
        tts = MyApplication.getInstance().getTts();
        Log.d("TTS", "TTS是否加载完成：" +tts.isLoaded());
        return root;
    }

    private void handleTextSize() {
        // 获取SharedPreferences
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("SeekbarPrefs", Context.MODE_PRIVATE);
        // 读取之前保存的position
        //如果没有存储值，默认为 2
        savedPosition = sharedPreferences.getInt("position", 2);

        tvBackground.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        btnSelectImage.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        btnSelectVideo.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        btnOpenCamera.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        tvSelectImage.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        tvSelectVideo.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        tvOpenCamera.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        binding.tvColum1.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        binding.tvColum2.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        binding.tvColum3.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        binding.tvColum4.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));

    }
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        viewModel.setThemeColor(themeColor);
        viewModel.getThemeColor().observe(getViewLifecycleOwner(), newColor -> {
            int color = ContextCompat.getColor(getContext(), newColor);  // 替换为新颜色
            ViewCompat.setBackgroundTintList(btnSelectImage, ColorStateList.valueOf(color));
            ViewCompat.setBackgroundTintList(btnSelectVideo, ColorStateList.valueOf(color));
            ViewCompat.setBackgroundTintList(btnOpenCamera, ColorStateList.valueOf(color));
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            assert data != null;
            Uri uri = data.getData();
            switch (requestCode) {
                case REQUEST_SELECT_IMAGE:
                    //停止播放
                    if(tts!=null){
                        Log.d("TTS", "停止播放");
                        tts.stopPlayback();
                    }
                    //关闭摄像机资源
                    if(cameraProcessor!=null) {
                        Log.d("CameraProcessor", "关闭摄像机");
                        cameraProcessor.closeCamera();
                    }
                    ivContent.setImageURI(uri);
                    recognizeToolForImage.removeTableRow(tableLayout);
                    tableLayout2.setVisibility(View.GONE);
                    tableLayout.setVisibility(View.VISIBLE);
                    // 异步执行识别任务
                    ProgressDialog progressDialog1 = new ProgressDialog(getContext());
                    ImageProcessor imageProcessor = new ImageProcessor(getContext(), ivContent,savedPosition,tableLayout,progressDialog1,recognizeToolForImage);
                    imageProcessor.processImage(uri);
                    // 获取图片文件路径
                    tvSelectImage.setText(getRealPathFromUri(getContext(), uri, "image").split("emulated/0/")[1]);
                    tvSelectVideo.setText("");
                    tvOpenCamera.setText("");
                    tvBackground.setVisibility(View.GONE);
                    ivContent.setVisibility(View.VISIBLE);
                    vvContent.setVisibility(View.GONE);
                    flTextureView.setVisibility(View.GONE);
                    break;
                case REQUEST_SELECT_VIDEO:
                    //停止播放
                    if(tts!=null){
                        Log.d("TTS", "停止播放");
                        tts.stopPlayback();
                    }
                    // 停止之前的帧处理线程，避免干扰
                    if (videoProcessor != null) {
                        videoProcessor.stopFrameProcessing();
                        vvContent.setBackground(null);  // 清除背景
                    }
                    //关闭摄像机资源
                    if(cameraProcessor!=null) {
                        Log.d("CameraProcessor", "关闭摄像机资源");
                        cameraProcessor.closeCamera();
                    }
                    vvContent.setVideoURI(uri);
                    // 设置视频播放器的准备监听器
                    vvContent.setOnPreparedListener(mp -> {
                        mp.setLooping(true); // 循环播放
                        vvContent.start();
                    });
                    recognizeToolForVideo.removeTableRow(tableLayout);
                    tableLayout2.setVisibility(View.GONE);
                    tableLayout.setVisibility(View.VISIBLE);
                    // 异步执行视频帧处理任务
                    ProgressDialog progressDialog2 = new ProgressDialog(getContext());
                    videoProcessor = new VideoProcessor(getContext(), vvContent, savedPosition,tableLayout,progressDialog2,recognizeToolForVideo);
                    videoProcessor.processVideo(uri);
                    // 获取视频文件路径
                    tvSelectVideo.setText(getRealPathFromUri(getContext(), uri, "video").split("emulated/0/")[1]);
                    tvSelectImage.setText("");
                    tvOpenCamera.setText("");
                    tvBackground.setVisibility(View.GONE);
                    ivContent.setVisibility(View.GONE);
                    vvContent.setVisibility(View.VISIBLE);
                    flTextureView.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    cameraProcessor.openCamera();
                } else {
                    // 如果权限被拒绝，处理相应逻辑
                    tvOpenCamera.setText("相机权限已被拒绝授权");
                    tvSelectImage.setText("");
                    tvSelectVideo.setText("");
                }
            }
    );

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(cameraProcessor!=null) {
            Log.d("CameraProcessor", "关闭摄像机资源");
            cameraProcessor.closeCamera();
        }
        binding = null;
    }

}