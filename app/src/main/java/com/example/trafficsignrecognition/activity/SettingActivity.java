package com.example.trafficsignrecognition.activity;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getCommonFontSize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.trafficsignrecognition.R;
import com.example.trafficsignrecognition.util.CameraProcessor;
import com.example.trafficsignrecognition.util.CustomSeekbar;
import com.example.trafficsignrecognition.util.SharedViewModel;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {


    private ImageButton imageButton;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch broadcast;
    private CustomSeekbar seekBar;
    private TextView fontPreview;
    private View selectedColorBlock;
    private View colorBlock1;
    private View colorBlock2;
    private View colorBlock3;
    private View colorBlock4;
    private View colorBlock5;
    private View colorBlock6;
    private View colorBlock7;
    private View colorBlock8;
    private View colorBlock9;
    private View colorBlock10;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //隐藏顶部栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        // 获取SharedPreferences
        SharedPreferences sharedPreferences1 = getSharedPreferences("SeekbarPrefs", Context.MODE_PRIVATE);
        int savedPosition = sharedPreferences1.getInt("position", 2);  // 如果没有存储值，默认为 2

        SharedPreferences sharedPreferences2 = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences2.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色

        SharedPreferences sharedPreferences3 = getSharedPreferences("Switch", Context.MODE_PRIVATE);
        boolean switchBroadcast = sharedPreferences3.getBoolean("broadcast", true);  // 如果没有存储值，默认为true
        //获取sharedViewModel
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        TextView tvSetting = findViewById(R.id.tv_setting);
        imageButton = findViewById(R.id.ib_back);
        tvSetting.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        //设置监听事件
        imageButton.setOnClickListener(view -> finish());

        TextView tvBroadcast = findViewById(R.id.tv_broadcast);
        broadcast = findViewById(R.id.switch_broadcast);
        tvBroadcast.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        broadcast.setChecked(switchBroadcast);
        broadcast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存当前isChecked到SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences3.edit();
            editor.putBoolean("broadcast", isChecked);
            editor.apply();
            //更新sharedViewModel
            sharedViewModel.setBroadcast(isChecked);
        });
        sharedViewModel.getBroadcast().observe(this, CameraProcessor::updateBroadcast);

        TextView tvFont = findViewById(R.id.tv_font);
        fontPreview = findViewById(R.id.tv_font_preview);
        seekBar = findViewById(R.id.progressBar);
        tvFont.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        fontPreview.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        seekBar.setCurrentProgress(savedPosition);  // 设置Seekbar到保存的位置
        seekBar.setOnPointResultListener(position -> {
            //Toast.makeText(getApplicationContext(), "圆点位置: " + position, Toast.LENGTH_SHORT).show();
            // 设置fontPreview的字体大小，sp单位
            fontPreview.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(position));
            // 保存当前position到SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences1.edit();
            editor.putInt("position", position);
            editor.apply();  // 提交保存
        });

        TextView tvTheme = findViewById(R.id.tv_theme);
        tvTheme.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));

        // 获取颜色块视图
        colorBlock1 = findViewById(R.id.color_block_1);
        colorBlock2 = findViewById(R.id.color_block_2);
        colorBlock3 = findViewById(R.id.color_block_3);
        colorBlock4 = findViewById(R.id.color_block_4);
        colorBlock5 = findViewById(R.id.color_block_5);
        colorBlock6 = findViewById(R.id.color_block_6);
        colorBlock7 = findViewById(R.id.color_block_7);
        colorBlock8 = findViewById(R.id.color_block_8);
        colorBlock9 = findViewById(R.id.color_block_9);
        colorBlock10 = findViewById(R.id.color_block_10);

        //设置初始主题颜色
        initThemeColor(themeColor);
        // 设置点击事件
        colorBlock1.setOnClickListener(v -> selectColorBlock(colorBlock1, R.color.green));
        colorBlock2.setOnClickListener(v -> selectColorBlock(colorBlock2, R.color.light_blue));
        colorBlock3.setOnClickListener(v -> selectColorBlock(colorBlock3, R.color.dark_blue));
        colorBlock4.setOnClickListener(v -> selectColorBlock(colorBlock4, R.color.orange));
        colorBlock5.setOnClickListener(v -> selectColorBlock(colorBlock5, R.color.red));
        colorBlock6.setOnClickListener(v -> selectColorBlock(colorBlock6, R.color.pink));
        colorBlock7.setOnClickListener(v -> selectColorBlock(colorBlock7, R.color.purple));
        colorBlock8.setOnClickListener(v -> selectColorBlock(colorBlock8, R.color.gray));
        colorBlock9.setOnClickListener(v -> selectColorBlock(colorBlock9, R.color.dark_green));
        colorBlock10.setOnClickListener(v -> selectColorBlock(colorBlock10, R.color.medium_blue));



        sharedViewModel.setThemeColor(themeColor);
        sharedViewModel.getThemeColor().observe(this, newColor -> {
            // 顶部栏颜色
            imageButton.setBackgroundColor(ContextCompat.getColor(this, newColor));
            ((LinearLayout)imageButton.getParent()).setBackgroundColor(ContextCompat.getColor(this, newColor));

            // 加载原始的track_on drawable
            GradientDrawable originalTrackOn = (GradientDrawable) Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.track_on)).mutate();
            // 创建一个新的GradientDrawable，复制原始drawable的属性
            GradientDrawable newTrackOn = new GradientDrawable();
            newTrackOn.setShape(originalTrackOn.getShape());
            newTrackOn.setCornerRadius(originalTrackOn.getCornerRadius());
            newTrackOn.setSize(originalTrackOn.getIntrinsicWidth(), originalTrackOn.getIntrinsicHeight());
            // 修改solid颜色
            int color = ContextCompat.getColor(this, newColor); // 替换为你想要的新颜色
            newTrackOn.setColor(color);
            // 创建一个新的selector drawable
            StateListDrawable selector = new StateListDrawable();
            selector.addState(new int[]{android.R.attr.state_checked}, newTrackOn); // 当开关打开时显示的背景
            selector.addState(new int[]{-android.R.attr.state_checked}, ContextCompat.getDrawable(this, R.drawable.track_off)); // 当开关关闭时显示的背景
            broadcast.setTrackDrawable(selector);
            // 强制 Switch 重新绘制
            broadcast.invalidate();
            // 如果需要，可以强制状态变化以确保颜色更新
            boolean isChecked = broadcast.isChecked();
            broadcast.setChecked(!isChecked);
            broadcast.setChecked(isChecked);
            //更新seekbar颜色
            seekBar.setLineColor(ContextCompat.getColor(this, newColor));
        });

        LinearLayout llExit = findViewById(R.id.ll_exit);
        llExit.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("确定退出该账号？");
            builder.setPositiveButton("是", (dialog, which) -> {
                // 获取SharedPreferences对象
                SharedPreferences preferences = this.getSharedPreferences("login", MODE_PRIVATE);
                // 获取SharedPreferences.Editor对象
                SharedPreferences.Editor editor = preferences.edit();
                // 清除保存的用户名和密码
                editor.remove("email");
                editor.remove("password");
                // 提交修改
                editor.apply();
                // 然后跳转到登录页面
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("source", "setting"); // 添加额外标识
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
            builder.setNegativeButton("否", null);
            builder.create().show();
        });
        TextView tvExit = findViewById(R.id.tv_exit);
        tvExit.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
    }

    @SuppressLint("NonConstantResourceId")
    private void initThemeColor(int themeColor) {
        LayerDrawable layerDrawable;
        GradientDrawable firstLayer;
        switch (themeColor) {
            case R.color.green:
                layerDrawable = (LayerDrawable) colorBlock1.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.green));
                selectedColorBlock = colorBlock1; // 记录当前选中的颜色块
                break;
            case R.color.light_blue:
                layerDrawable = (LayerDrawable) colorBlock2.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.light_blue));
                selectedColorBlock = colorBlock2; // 记录当前选中的颜色块
                break;
            case R.color.dark_blue:
                layerDrawable = (LayerDrawable) colorBlock3.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.dark_blue));
                selectedColorBlock = colorBlock3; // 记录当前选中的颜色块
                break;
            case R.color.orange:
                layerDrawable = (LayerDrawable) colorBlock4.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.orange));
                selectedColorBlock = colorBlock4; // 记录当前选中的颜色块
                break;
            case R.color.red:
                layerDrawable = (LayerDrawable) colorBlock5.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.red));
                selectedColorBlock = colorBlock5; // 记录当前选中的颜色块
                break;
            case R.color.pink:
                layerDrawable = (LayerDrawable) colorBlock6.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.pink));
                selectedColorBlock = colorBlock6; // 记录当前选中的颜色块
                break;
            case R.color.purple:
                layerDrawable = (LayerDrawable) colorBlock7.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.purple));
                selectedColorBlock = colorBlock7; // 记录当前选中的颜色块
                break;
            case R.color.gray:
                layerDrawable = (LayerDrawable) colorBlock8.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.gray));
                selectedColorBlock = colorBlock8; // 记录当前选中的颜色块
                break;
            case R.color.dark_green:
                layerDrawable = (LayerDrawable) colorBlock9.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.dark_green));
                selectedColorBlock = colorBlock9; // 记录当前选中的颜色块
                break;
            case R.color.medium_blue:
                layerDrawable = (LayerDrawable) colorBlock10.getBackground();
                firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
                firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.medium_blue));
                selectedColorBlock = colorBlock10; // 记录当前选中的颜色块
                break;
            default:
                break;
        }
        // 加载原始的track_on drawable
        GradientDrawable originalTrackOn = (GradientDrawable) Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.track_on)).mutate();
        // 创建一个新的GradientDrawable，复制原始drawable的属性
        GradientDrawable newTrackOn = new GradientDrawable();
        newTrackOn.setShape(originalTrackOn.getShape());
        newTrackOn.setCornerRadius(originalTrackOn.getCornerRadius());
        newTrackOn.setSize(originalTrackOn.getIntrinsicWidth(), originalTrackOn.getIntrinsicHeight());
        // 修改solid颜色
        int color = ContextCompat.getColor(this, themeColor); // 替换为新颜色
        newTrackOn.setColor(color);
        // 创建一个新的selector drawable
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_checked}, newTrackOn); // 当开关打开时显示的背景
        selector.addState(new int[]{-android.R.attr.state_checked}, ContextCompat.getDrawable(this, R.drawable.track_off)); // 当开关关闭时显示的背景
        broadcast.setTrackDrawable(selector);
    }

    private void selectColorBlock(View colorBlock, int borderColor) {
        // 如果有之前选中的颜色块，取消边框颜色
        if (selectedColorBlock != null) {
            LayerDrawable layerDrawable = (LayerDrawable) selectedColorBlock.getBackground();
            GradientDrawable firstLayer = (GradientDrawable) layerDrawable.getDrawable(0);
            firstLayer.setStroke(7, ContextCompat.getColor(this, R.color.background_grey));
        }
        // 设置当前颜色块为选中状态，并应用边框颜色
        LayerDrawable layerDrawable1 = (LayerDrawable) colorBlock.getBackground();
        GradientDrawable firstLayer1 = (GradientDrawable) layerDrawable1.getDrawable(0);
        firstLayer1.setStroke(7, ContextCompat.getColor(this, borderColor));
        selectedColorBlock = colorBlock; // 记录当前选中的颜色块
        //保存主题颜色
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("themeColor", borderColor);
        editor.apply();
        // 更新 SharedViewModel
        sharedViewModel.setThemeColor(borderColor);
    }
}