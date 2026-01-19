package com.example.trafficsignrecognition.activity;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getAlertSize;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getCommonFontSize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.trafficsignrecognition.R;
import com.example.trafficsignrecognition.dto.UserDTO;
import com.example.trafficsignrecognition.httprequest.ApiClient;
import com.example.trafficsignrecognition.httprequest.ApiService;
import com.example.trafficsignrecognition.util.CodeUtil;
import com.example.trafficsignrecognition.util.PasswordUtil;
import com.example.trafficsignrecognition.util.SharedViewModel;
import com.example.trafficsignrecognition.util.ToastUtil;
import com.example.trafficsignrecognition.util.UsernameUtil;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText et_email;
    private Spinner spinner;
    private EditText et_password;
    private EditText et_validCode;
    private ImageView iv_validCode;
    private TextView tv_forgetPassword;
    private Button btn_login;
    private TextView tv_sign_up;
    private Button btn_sign_up;
    private String realCode;
    private ApiService apiService;
    private String source;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //隐藏顶部栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 获取Intent中的额外信息
        source = getIntent().getStringExtra("source");

        //获取各个控件
        et_email = findViewById(R.id.et_email2);
        spinner = findViewById(R.id.spinner_email_provider2);
        et_password = findViewById(R.id.et_password);
        et_validCode = findViewById(R.id.et_validCode);
        iv_validCode = findViewById(R.id.iv_validCode);
        tv_forgetPassword = findViewById(R.id.tv_forgetPassword);
        btn_login = findViewById(R.id.btn_login);
        tv_sign_up = findViewById(R.id.tv_sign_up);
        btn_sign_up = findViewById(R.id.btn_sign_up);
        handleTextSize();

        // 获取Retrofit API服务实例
        apiService = ApiClient.getClient().create(ApiService.class);
        tv_forgetPassword.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_sign_up.setOnClickListener(this);
        iv_validCode.setOnClickListener(this);

        //将验证码用图片的形式显示出来
        iv_validCode.setImageBitmap(CodeUtil.getInstance().createBitmap());
        realCode = CodeUtil.getInstance().getCode().toLowerCase();

        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        SharedViewModel themeViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);
        themeViewModel.getThemeColor().observe(this, newColor -> {
            tv_forgetPassword.setHintTextColor(getResources().getColor(newColor));
            int color = ContextCompat.getColor(this, newColor);
            ViewCompat.setBackgroundTintList(btn_login, ColorStateList.valueOf(color));
            ViewCompat.setBackgroundTintList(btn_sign_up, ColorStateList.valueOf(color));
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
            tv_forgetPassword.setHintTextColor(getResources().getColor(newColor));
            int color = ContextCompat.getColor(this, newColor);
            ViewCompat.setBackgroundTintList(btn_login, ColorStateList.valueOf(color));
            ViewCompat.setBackgroundTintList(btn_sign_up, ColorStateList.valueOf(color));
        });
    }

    private void handleTextSize() {
        SharedPreferences sharedPreferences = getSharedPreferences("SeekbarPrefs", Context.MODE_PRIVATE);
        // 读取之前保存的position
        int savedPosition = sharedPreferences.getInt("position", 2);  // 如果没有存储值，默认为 2

        et_email.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        et_password.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        et_validCode.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        tv_forgetPassword.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        btn_login.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        tv_sign_up.setTextSize(COMPLEX_UNIT_SP,getAlertSize(savedPosition));
        btn_sign_up.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        // 设置Spinner的字体大小以及下拉框内容
        setupSpinner(savedPosition);
    }

    private void setupSpinner(int savedPosition) {
        // 自定义的 ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.custom_spinner_item,   // 自定义布局
                getResources().getStringArray(R.array.email_providers)) {  // 数据源

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                // 设置 Spinner 展示的字体大小
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.spinner_text);
                textView.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition)); // 使用动态字体大小
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                // 设置下拉项的字体大小
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.spinner_text);
                textView.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition)); // 使用动态字体大小
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.custom_spinner_item);  // 使用自定义的下拉项布局
        spinner.setAdapter(adapter);  // 设置适配器
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                handleLogin();
                break;
            case R.id.btn_sign_up:
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.iv_validCode:  //改变随机验证码的生成
                iv_validCode.setImageBitmap(CodeUtil.getInstance().createBitmap());
                realCode = CodeUtil.getInstance().getCode().toLowerCase();
                break;
            case R.id.tv_forgetPassword:
                Intent intent2 = new Intent(LoginActivity.this,ForgetPasswordActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                finish();
                break;
            default:
                break;
        }
    }

    //点击登录需要处理的事件
    private void handleLogin() {
        String email_prefix = et_email.getText().toString().trim();
        String email = email_prefix + spinner.getSelectedItem().toString();
        String password = et_password.getText().toString().trim();
        String code = et_validCode.getText().toString().toLowerCase();
        if (!isValidEmail(email)) {
            ToastUtil.show(this, "邮箱地址不合法!");
            return;
        }
        if (!PasswordUtil.isPasswordValid(password)) {
            ToastUtil.show(this, "密码至少六位，必须且只包含大小写字母以及数字!");
            return;
        }
        if (!code.equals(realCode)) {
            //刷新验证码
            iv_validCode.setImageBitmap(CodeUtil.getInstance().createBitmap());
            realCode = CodeUtil.getInstance().getCode().toLowerCase();
            ToastUtil.show(this, "验证码错误!");
            return;
        }
        UserDTO userDTO = new UserDTO(email, password);
        // 调用Retrofit接口发起登录请求
        Call<String> call = apiService.login(userDTO);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    // 登录成功
                    String result = response.body();
                    saveUserInfo(email,password);
                    Toast.makeText(LoginActivity.this, result, Toast.LENGTH_LONG).show();
                    //跳转至主页面
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    //栈中存在待跳转的的活动实例时，则重新创建该活动的实例，并清除原实例上方的所有实例
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    // 登录失败，获取错误信息
                    try {
                        // 将errorBody转换为字符串，显示服务器返回的具体错误信息
                        assert response.errorBody() != null;
                        String errorMessage = response.errorBody().string();
                        Toast.makeText(LoginActivity.this, "登录失败：" + errorMessage, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(LoginActivity.this, "登录失败：无法读取错误消息", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                // 请求失败
                Toast.makeText(LoginActivity.this, "请求失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("LoginActivity", "Error sending email: " + t.getMessage());
            }
        });
    }

    private void saveUserInfo(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        if(source.equals("signup")){
            //生成随机的用户名
            String username = "用户" + UsernameUtil.getStringRandom(6);
            //初始化头像路径
            String imagePath = "null";
            editor.putString("username", username);
            editor.putString("imagePath", imagePath);
        }
        editor.apply();
    }


    // 验证邮箱地址是否合法
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    //判断该事件是否需要收起软键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //如果是点击事件，获取点击的view，并判断是否要收起键盘
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //获取目前得到焦点的view
            View v = getCurrentFocus();
            //判断是否要收起并进行处理
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        //这个是activity的事件分发，一定要有，不然就不会有任何的点击事件了
        return super.dispatchTouchEvent(ev);
    }

    //判断是否要收起键盘
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        //如果目前得到焦点的这个view是editText的话进行判断点击的位置
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            // 点击EditText的事件，忽略它。
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略
        return false;
    }

    //隐藏软键盘并让editText失去焦点
    private void hideKeyboard(IBinder token) {
        et_email.clearFocus();
        et_password.clearFocus();
        if (token != null) {
            //这里先获取InputMethodManager再调用他的方法来关闭软键盘
            //InputMethodManager就是一个管理窗口输入的manager
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

}
