package com.example.trafficsignrecognition.activity;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getAlertSize;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getCommonFontSize;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.trafficsignrecognition.R;
import com.example.trafficsignrecognition.dto.UpdatePasswordDTO;
import com.example.trafficsignrecognition.httprequest.ApiClient;
import com.example.trafficsignrecognition.httprequest.ApiService;
import com.example.trafficsignrecognition.util.PasswordUtil;
import com.example.trafficsignrecognition.util.SharedViewModel;
import com.example.trafficsignrecognition.util.ToastUtil;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UpdatePasswordActivity extends AppCompatActivity {

    private EditText etOldPassword;
    private LinearLayout alertOldPassword;
    private EditText etNewPassword;
    private LinearLayout alertValidPassword1;
    private EditText etConfirmPassword;
    private LinearLayout alertValidPassword2;
    private LinearLayout alertNewPassword;
    private TextView tvOldPassword;
    private TextView tvAlertOldPassword;
    private TextView tvNewPassword;
    private TextView tvAlertValidPassword1;
    private TextView tvConfirmPasswordById;
    private TextView tvAlertValidPassword2;
    private TextView tvAlertNewPassword;
    private Button btnConfirmUpdate;
    private TextView tvUpdatePassword;
    private ImageButton ibBack2;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        //隐藏顶部栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 当前在MainActivity,获取SharedPreferences对象
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        // 获取保存的邮箱和密码
        String email = sharedPreferences.getString("email", "");
        String password = sharedPreferences.getString("password", "");

        ibBack2 = findViewById(R.id.ib_back2);
        tvUpdatePassword = findViewById(R.id.tv_updatePassword);
        tvOldPassword = findViewById(R.id.tv_oldPassword);
        etOldPassword = findViewById(R.id.et_oldPassword);
        tvAlertOldPassword = findViewById(R.id.tv_alert_oldPassword);
        alertOldPassword = findViewById(R.id.alert_oldPassword);
        tvNewPassword = findViewById(R.id.tv_newPassword);
        etNewPassword = findViewById(R.id.et_newPassword);
        tvAlertValidPassword1 = findViewById(R.id.tv_alert_validPassword1);
        alertValidPassword1 = findViewById(R.id.alert_validPassword1);
        tvConfirmPasswordById = findViewById(R.id.tv_confirmPassword);
        etConfirmPassword = findViewById(R.id.et_confirmPassword);
        tvAlertValidPassword2 = findViewById(R.id.tv_alert_validPassword2);
        alertValidPassword2 = findViewById(R.id.alert_validPassword2);
        tvAlertNewPassword = findViewById(R.id.tv_alert_newPassword);
        alertNewPassword = findViewById(R.id.alert_newPassword);
        btnConfirmUpdate = findViewById(R.id.btn_confirmUpdate);
        handleTextSize();

        // 获取Retrofit API服务实例
        apiService = ApiClient.getClient().create(ApiService.class);

        ibBack2.setOnClickListener(view -> {
            //结束当前Activity
            finish();
        });
        etOldPassword.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                assert password != null;
                if (password.equals(etOldPassword.getText().toString()))
                    alertOldPassword.setVisibility(View.GONE);
                else
                    alertOldPassword.setVisibility(View.VISIBLE);
            }
        });
        etNewPassword.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                if (PasswordUtil.isPasswordValid(etNewPassword.getText().toString()))
                    alertValidPassword1.setVisibility(View.GONE);
                else
                    alertValidPassword1.setVisibility(View.VISIBLE);
            }
        });
        etConfirmPassword.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                if (etConfirmPassword.getText().toString().equals(etNewPassword.getText().toString()))
                    alertNewPassword.setVisibility(View.GONE);
                else
                    alertNewPassword.setVisibility(View.VISIBLE);
                if (PasswordUtil.isPasswordValid(etConfirmPassword.getText().toString()))
                    alertValidPassword2.setVisibility(View.GONE);
                else
                    alertValidPassword2.setVisibility(View.VISIBLE);
            }
        });
        btnConfirmUpdate.setOnClickListener(view -> {
            assert password != null;
            if (!(password.equals(etOldPassword.getText().toString()))) {
                ToastUtil.show(this, "输入的旧密码有误，请重新输入");
            } else {
                if (!(etConfirmPassword.getText().toString().equals(etNewPassword.getText().toString())))
                    ToastUtil.show(this, "两次输入的新密码不一致，请重新输入");
                else {
                    if (PasswordUtil.isPasswordValid(etNewPassword.getText().toString()) && PasswordUtil.isPasswordValid(etConfirmPassword.getText().toString())) {
                        //修改远程Mysql的密码
                        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO();
                        updatePasswordDTO.setEmail(email);
                        updatePasswordDTO.setOldPassword(etOldPassword.getText().toString());
                        updatePasswordDTO.setNewPassword(etConfirmPassword.getText().toString());

                        Call<String> call = apiService.updatePassword(updatePasswordDTO);
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if (response.isSuccessful()) {
                                    String result = response.body();
                                    // 获取 SharedPreferences 对象
                                    SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
                                    // 获取 SharedPreferences.Editor 对象
                                    SharedPreferences.Editor editor = preferences.edit();
                                    // 修改 password 的值
                                    editor.putString("password", etConfirmPassword.getText().toString());
                                    // 提交更改
                                    editor.apply();
                                    alertNewPassword.setVisibility(View.GONE);
                                    alertOldPassword.setVisibility(View.GONE);
                                    alertValidPassword1.setVisibility(View.GONE);
                                    alertValidPassword2.setVisibility(View.GONE);
                                    Toast.makeText(UpdatePasswordActivity.this, result, Toast.LENGTH_LONG).show();
                                } else {
                                    Log.d("UpdatePasswordActivity", "Error Code: " + response.code());
                                    Log.d("UpdatePasswordActivity", "Response Headers: " + response.headers());
                                    try {
                                        assert response.errorBody() != null;
                                        String errorMessage = response.errorBody().string();
                                        Log.d("UpdatePasswordActivity", "Error Body: " + errorMessage);
                                        Toast.makeText(UpdatePasswordActivity.this, "修改密码失败：" + errorMessage, Toast.LENGTH_LONG).show();
                                    } catch (IOException e) {
                                        Toast.makeText(UpdatePasswordActivity.this, "修改密码失败: 无法读取错误消息", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                Toast.makeText(UpdatePasswordActivity.this, "请求失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else
                        ToastUtil.show(this, "密码至少六位，必须且只包含大小写字母以及数字!");
                }
            }
        });

        SharedPreferences sharedPreferences2 = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences2.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        SharedViewModel themeViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);
        themeViewModel.getThemeColor().observe(this, newColor -> {
            // 顶部栏颜色
            ibBack2.setBackgroundColor(ContextCompat.getColor(this, newColor));
            ((LinearLayout) ibBack2.getParent()).setBackgroundColor(ContextCompat.getColor(this, newColor));
            int color = ContextCompat.getColor(this, newColor);
            ViewCompat.setBackgroundTintList(btnConfirmUpdate, ColorStateList.valueOf(color));

            // 动态创建新的 GradientDrawable 用于选中状态
            GradientDrawable focusedDrawable = new GradientDrawable();
            focusedDrawable.setColor(Color.WHITE);  // 背景色为白色
            focusedDrawable.setStroke(5, ContextCompat.getColor(this, newColor));  // 边框颜色为主题色
            focusedDrawable.setCornerRadius(7f);  // 圆角半径为 5dp
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                focusedDrawable.setPadding(3, 3, 3, 3);  // 设置内边距
            }

            // 创建未选中状态的 GradientDrawable，严格按照 shape_edit_normal.xml 样式
            GradientDrawable normalDrawable = new GradientDrawable();
            normalDrawable.setColor(Color.WHITE);  // 背景色为白色
            normalDrawable.setStroke(3, ContextCompat.getColor(this, R.color.grey));  // 默认边框颜色为灰色
            normalDrawable.setCornerRadius(7f);  // 圆角半径为 5dp
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                normalDrawable.setPadding(3, 3, 3, 3);  // 设置内边距
            }

            // 创建 StateListDrawable 并添加状态
            StateListDrawable stateListDrawable1 = new StateListDrawable();
            stateListDrawable1.addState(new int[]{android.R.attr.state_focused}, focusedDrawable);  // 选中状态
            stateListDrawable1.addState(new int[]{}, normalDrawable);  // 默认状态
            etOldPassword.setBackground(stateListDrawable1);

            StateListDrawable stateListDrawable2 = new StateListDrawable();
            stateListDrawable2.addState(new int[]{android.R.attr.state_focused}, focusedDrawable);  // 选中状态
            stateListDrawable2.addState(new int[]{}, normalDrawable);  // 默认状态
            etNewPassword.setBackground(stateListDrawable2);

            StateListDrawable stateListDrawable3 = new StateListDrawable();
            stateListDrawable3.addState(new int[]{android.R.attr.state_focused}, focusedDrawable);  // 选中状态
            stateListDrawable3.addState(new int[]{}, normalDrawable);  // 默认状态
            etConfirmPassword.setBackground(stateListDrawable3);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        SharedViewModel themeViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);
        themeViewModel.getThemeColor().observe(this, newColor -> {
            // 顶部栏颜色
            ibBack2.setBackgroundColor(ContextCompat.getColor(this, newColor));
            ((LinearLayout) ibBack2.getParent()).setBackgroundColor(ContextCompat.getColor(this, newColor));
            int color = ContextCompat.getColor(this, newColor);
            ViewCompat.setBackgroundTintList(btnConfirmUpdate, ColorStateList.valueOf(color));

            // 动态创建新的 GradientDrawable 用于选中状态
            GradientDrawable focusedDrawable = new GradientDrawable();
            focusedDrawable.setColor(Color.WHITE);  // 背景色为白色
            focusedDrawable.setStroke(5, ContextCompat.getColor(this, newColor));  // 边框颜色为主题色
            focusedDrawable.setCornerRadius(7f);  // 圆角半径为 5dp
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                focusedDrawable.setPadding(3, 3, 3, 3);  // 设置内边距
            }

            // 创建未选中状态的 GradientDrawable，严格按照 shape_edit_normal.xml 样式
            GradientDrawable normalDrawable = new GradientDrawable();
            normalDrawable.setColor(Color.WHITE);  // 背景色为白色
            normalDrawable.setStroke(3, ContextCompat.getColor(this, R.color.grey));  // 默认边框颜色为灰色
            normalDrawable.setCornerRadius(7f);  // 圆角半径为 5dp
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                normalDrawable.setPadding(3, 3, 3, 3);  // 设置内边距
            }

            // 创建 StateListDrawable 并添加状态
            StateListDrawable stateListDrawable1 = new StateListDrawable();
            stateListDrawable1.addState(new int[]{android.R.attr.state_focused}, focusedDrawable);  // 选中状态
            stateListDrawable1.addState(new int[]{}, normalDrawable);  // 默认状态
            etOldPassword.setBackground(stateListDrawable1);

            StateListDrawable stateListDrawable2 = new StateListDrawable();
            stateListDrawable2.addState(new int[]{android.R.attr.state_focused}, focusedDrawable);  // 选中状态
            stateListDrawable2.addState(new int[]{}, normalDrawable);  // 默认状态
            etNewPassword.setBackground(stateListDrawable2);

            StateListDrawable stateListDrawable3 = new StateListDrawable();
            stateListDrawable3.addState(new int[]{android.R.attr.state_focused}, focusedDrawable);  // 选中状态
            stateListDrawable3.addState(new int[]{}, normalDrawable);  // 默认状态
            etConfirmPassword.setBackground(stateListDrawable3);
        });
    }

    private void handleTextSize() {
        // 获取SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("SeekbarPrefs", Context.MODE_PRIVATE);
        // 读取之前保存的position
        int savedPosition = sharedPreferences.getInt("position", 2);  // 如果没有存储值，默认为 2

        tvUpdatePassword.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        tvOldPassword.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        etOldPassword.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        tvAlertOldPassword.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        tvNewPassword.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        etNewPassword.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        tvAlertValidPassword1.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        tvConfirmPasswordById.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        etConfirmPassword.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        tvAlertValidPassword2.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        tvAlertNewPassword.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        btnConfirmUpdate.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
    }

}