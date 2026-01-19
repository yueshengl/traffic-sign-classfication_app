package com.example.trafficsignrecognition.activity;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getCommonFontSize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.trafficsignrecognition.R;
import com.example.trafficsignrecognition.event.ImagePathChangedEvent;
import com.example.trafficsignrecognition.event.UsernameChangedEvent;
import com.example.trafficsignrecognition.util.ImageChooseUtil;
import com.example.trafficsignrecognition.util.Permission;
import com.example.trafficsignrecognition.util.SharedViewModel;
import com.example.trafficsignrecognition.util.UsernameUtil;
import com.google.android.material.navigation.NavigationView;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.hdodenhof.circleimageview.CircleImageView;


//应用程序APP的入口
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private CircleImageView iv_profileImage;
    private TextView tv_username;
    // 定义请求码
    private static final int REQUEST_CODE_TAKE_PHOTO = 1;
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 2;
    private static final int REQUEST_CODE_CROP_IMAGE = 3;
    private Toolbar toolbar;
    private ImageButton ib_setting;
    private View headerView;
    private NavigationView navigationView;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 注册EventBus
        EventBus.getDefault().register(this);

        // 当前在MainActivity,获取SharedPreferences对象
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        // 获取保存的邮箱和密码
        String email = preferences.getString("email", "");
        String password = preferences.getString("password", "");
        // 如果用户名和密码均空，跳到登陆界面
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("source", "main"); // 添加额外标识
            //栈中存在待跳转的的活动实例时，则重新创建该活动的实例，并清除原实例上方的所有实例
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //跳转到LoginActivity
            startActivity(intent);
            finish();
        }


        SharedPreferences preferences2 = getSharedPreferences("SeekbarPrefs", Context.MODE_PRIVATE);
        int savedPosition = preferences2.getInt("position", 2);  // 如果没有存储值，默认为 2
        toolbar = findViewById(R.id.app_bar_main).findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setToolbarTitleSize(toolbar, savedPosition);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //获取 ib_setting
        ib_setting = findViewById(R.id.app_bar_main).findViewById(R.id.ib_setting);
        //设置监听事件
        ib_setting.setOnClickListener(view -> {
            //跳转至SettingActivity
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        });
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // 从NavigationView中获取头部视图
        headerView = navigationView.getHeaderView(0);
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SpannableString spanString = new SpannableString(menuItem.getTitle());
            spanString.setSpan(new AbsoluteSizeSpan(getCommonFontSize(savedPosition), true), 0, spanString.length(), 0);
            menuItem.setTitle(spanString);
        }
        // 从头部视图中找到iv_profileImage和tv_username
        iv_profileImage = headerView.findViewById(R.id.profile_image);
        tv_username = headerView.findViewById(R.id.username);
        // 设置点击监听器
        iv_profileImage.setOnClickListener(view -> ImageChooseUtil.showPhotoOptionsDialog(this));
        tv_username.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));

        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        // 如果没有存储值，默认为绿色
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);
        SharedViewModel themeViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);
        themeViewModel.setToolbar(toolbar);
        themeViewModel.getThemeColor().observe(this, newColor -> {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, newColor));
            ib_setting.setBackgroundColor(ContextCompat.getColor(this, newColor));
            headerView.setBackgroundColor(ContextCompat.getColor(this, newColor));
            navigationView.setItemIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, newColor)));
        });
    }


    private void setToolbarTitleSize(Toolbar toolbar, Integer position) {
        toolbar.post(() -> {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View view = toolbar.getChildAt(i);
                if (view instanceof TextView) {
                    TextView toolbarTitle = (TextView) view;
                    // 设置字体大小，单位是SP
                    toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, getCommonFontSize(position));
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUsernameChanged(UsernameChangedEvent event) {
        tv_username.setText(event.getUsername());
    }


    //回调方法
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CHOOSE_PHOTO:
                    // 选择照片完成后进行裁剪
                    if (data != null) {
                        ImageChooseUtil.cropImage(data.getData(), this);
                    }
                    break;
                case REQUEST_CODE_CROP_IMAGE:
                    //裁剪完图片后
                    Log.d("dai", "裁剪已完成");
                    //检查是否有权限
                    Permission.checkPermission(this);
                    // 使用Glide加载裁剪后的图片到缩略图iv_profileImage中
                    Uri croppedUri = UCrop.getOutput(data);
                    assert croppedUri != null;
                    String path = croppedUri.toString();
                    updateImagePath(path);
                    //通过事件总线更改头像路径
                    EventBus.getDefault().post(new ImagePathChangedEvent(path));
                    Glide.with(this).load(croppedUri).into(iv_profileImage);
                    break;
                case REQUEST_CODE_TAKE_PHOTO:
                    // 拍照完成后进行裁剪
                    ImageChooseUtil.cropImage(ImageChooseUtil.getPhotoURI(), this);
                    break;
                default:
                    break;
            }
        }
    }

    private void updateImagePath(String path) {
        // 获取 SharedPreferences 对象
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        // 获取 SharedPreferences.Editor 对象
        SharedPreferences.Editor editor = preferences.edit();
        // 修改 imagePath 的值
        editor.putString("imagePath", path);
        // 提交更改
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Permission.checkPermission(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        // 获取保存的邮箱和密码
        String username = preferences.getString("username", "用户9999zz");
        String imagePath = preferences.getString("imagePath", "null");
        //设置用户名
        tv_username.setText(username);
        //设置头像
        assert imagePath != null;
        if (!imagePath.equals("null")) {
            Log.d("imagePath", imagePath);
            refreshImage(imagePath);
        } else {
            Glide.with(this)
                    .load(R.drawable.headshot)
                    .into(iv_profileImage);
        }
        if (Permission.isPermissionGranted(this)) {
            Log.i("Permission", "请求权限成功");
        }
    }


    //检查是否授权成功，失败则退出应用
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Permission.REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            StringBuilder failedPermissions = new StringBuilder();

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    failedPermissions.append(permissions[i]).append("\n");
                }
            }

            if (!allPermissionsGranted) {
                String message = "以下权限未授权成功：" + failedPermissions.toString().trim().replaceAll(", $", "");
                Log.e("Permission", message);
                // 显示弹窗
                showPermissionDialog(message);
            }
        }
    }

    private void showPermissionDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("权限提示")
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 用户点击了确定按钮
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    //刷新头像
    private void refreshImage(String imagePath) {
        //检查是否有权限
        //Permission.checkPermission(this);
        Glide.with(this).load(imagePath).into(iv_profileImage);
    }

}