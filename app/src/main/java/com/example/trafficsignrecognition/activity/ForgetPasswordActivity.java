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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.trafficsignrecognition.R;
import com.example.trafficsignrecognition.dto.ResetPasswordDTO;
import com.example.trafficsignrecognition.httprequest.ApiClient;
import com.example.trafficsignrecognition.httprequest.ApiService;
import com.example.trafficsignrecognition.util.ConstantUtil;
import com.example.trafficsignrecognition.util.PasswordUtil;
import com.example.trafficsignrecognition.util.SharedViewModel;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetPasswordActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private EditText et_email;
    private Spinner spinner;
    private EditText et_email_verification_code;
    private Button btn_send_code;
    private EditText et_password1;
    private LinearLayout ll_alert_validPassword;
    private TextView alert_validPassword1;
    private EditText et_password2;
    private LinearLayout ll_alert_validPassword2;
    private TextView alert_validPassword2;
    private LinearLayout alert_newPassword1;
    private TextView alert_newPassword2;
    private TextView tv_returnLogin;
    private Button reset_password;
    private String generatedCode;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        //隐藏顶部栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //获取各个控件
        et_email = findViewById(R.id.et_email3);
        spinner = findViewById(R.id.spinner_email_provider3);
        et_email_verification_code = findViewById(R.id.et_email_verification_code2);
        btn_send_code = findViewById(R.id.btn_send_code2);
        et_password1 = findViewById(R.id.et_password4);
        ll_alert_validPassword = findViewById(R.id.ll_alert_validPassword3);
        alert_validPassword1 = findViewById(R.id.alert_validPassword3);
        et_password2 = findViewById(R.id.et_password5);
        ll_alert_validPassword2 = findViewById(R.id.ll_alert_validPassword4);
        alert_validPassword2 = findViewById(R.id.alert_validPassword4);
        alert_newPassword1 = findViewById(R.id.alert_newPassword3);
        alert_newPassword2 = findViewById(R.id.alert_newPassword4);
        tv_returnLogin = findViewById(R.id.tv_returnLogin2);
        reset_password = findViewById(R.id.reset_password);
        handleTextSize();

        // 获取Retrofit API服务实例
        apiService = ApiClient.getClient().create(ApiService.class);
        btn_send_code.setOnClickListener(this);
        tv_returnLogin.setOnClickListener(this);
        reset_password.setOnClickListener(this);
        et_password1.setOnFocusChangeListener(this);
        et_password2.setOnFocusChangeListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        SharedViewModel themeViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);
        themeViewModel.getThemeColor().observe(this, newColor -> {
            tv_returnLogin.setHintTextColor(getResources().getColor(newColor));
            int color = ContextCompat.getColor(this, newColor);
            ViewCompat.setBackgroundTintList(btn_send_code, ColorStateList.valueOf(color));
            ViewCompat.setBackgroundTintList(reset_password, ColorStateList.valueOf(color));
        });
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_send_code2:
                sendVerificationCode();
                break;
            case R.id.reset_password:
                handleReset();
                break;
            case R.id.tv_returnLogin2:
                Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                intent.putExtra("source", "forgetPassword"); // 添加额外标识
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            default:
                break;
        }
    }

    private void handleReset() {
        String email_prefix = et_email.getText().toString().trim();
        String email = email_prefix + spinner.getSelectedItem().toString();
        String enteredCode = et_email_verification_code.getText().toString().trim();
        String password = et_password2.getText().toString().trim();
        if (!isValidEmail(email)) {
            Toast.makeText(this, "邮箱地址不合法!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!enteredCode.equals(generatedCode)) {
            Toast.makeText(this, "邮箱验证码错误!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PasswordUtil.isPasswordValid(et_password1.getText().toString())) {
            ll_alert_validPassword.setVisibility(View.VISIBLE);
            return;
        }
        if (!et_password1.getText().toString().equals(et_password2.getText().toString())) {
            alert_newPassword1.setVisibility(View.VISIBLE);
            return;
        }
        //修改远程Mysql的密码
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setEmail(email);
        resetPasswordDTO.setNewPassword(et_password2.getText().toString());

        Call<String> call = apiService.resetPassword(resetPasswordDTO);
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
                    editor.putString("password", password);
                    // 提交更改
                    editor.apply();
                    ll_alert_validPassword.setVisibility(View.GONE);
                    ll_alert_validPassword2.setVisibility(View.GONE);
                    alert_newPassword1.setVisibility(View.GONE);
                    Toast.makeText(ForgetPasswordActivity.this, result, Toast.LENGTH_LONG).show();
                } else {
                    Log.d("ForgetPasswordActivity", "Error Code: " + response.code());
                    Log.d("ForgetPasswordActivity", "Response Headers: " + response.headers());
                    try {
                        assert response.errorBody() != null;
                        String errorMessage = response.errorBody().string();
                        Log.d("ForgetPasswordActivity", "Error Body: " + errorMessage);
                        Toast.makeText(ForgetPasswordActivity.this, "重置密码失败：" + errorMessage, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(ForgetPasswordActivity.this, "重置密码失败: 无法读取错误消息", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(ForgetPasswordActivity.this, "请求失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @SuppressLint("DefaultLocale")
    private void sendVerificationCode() {
        String email_prefix = et_email.getText().toString().trim();
        String email = email_prefix + spinner.getSelectedItem().toString();
        if (!isValidEmail(email)) {
            Toast.makeText(this, "邮箱地址不合法!", Toast.LENGTH_SHORT).show();
            return;
        }
        generatedCode = String.format("%06d", new Random().nextInt(999999));
        // 发送验证码到用户的邮箱
        new Thread(() -> {
            try {
                sendEmail(email, generatedCode);
                runOnUiThread(() -> Toast.makeText(this, "验证码已发送至 " + email, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.d("ForgetPasswordActivity", "Error sending email: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "发送验证码失败，请重试!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // 验证邮箱地址是否合法
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // 发送邮件的功能
    private void sendEmail(String recipientEmail, String verificationCode) throws MessagingException {
        String user = ConstantUtil.EMAIL_USER; // 发件人邮箱地址
        String password = ConstantUtil.EMAIL_PASSWORD; // 发件人邮箱密码
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.163.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true"); // 启用SSL加密
        Session session = Session.getDefaultInstance(properties, authenticator);
        Message message = new MimeMessage(session);
        Address from = new InternetAddress(user);//发送者地址
        message.setFrom(from);
        Address to = new InternetAddress(recipientEmail);
        message.setRecipient(Message.RecipientType.TO, to);//接收者地址
        message.setSubject("重置密码-验证码");
        message.setSentDate(new Date());
        message.setText(recipientEmail + ",您好!" + "\n" + "您正在对交通标志识别助手的密码进行重置，您的验证码是: " + verificationCode);
        Transport.send(message);//发送邮件
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        SharedViewModel themeViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);
        themeViewModel.getThemeColor().observe(this, newColor -> {
            tv_returnLogin.setHintTextColor(getResources().getColor(newColor));
            int color = ContextCompat.getColor(this, newColor);
            ViewCompat.setBackgroundTintList(btn_send_code, ColorStateList.valueOf(color));
            ViewCompat.setBackgroundTintList(reset_password, ColorStateList.valueOf(color));
        });
    }

    private void handleTextSize() {
        SharedPreferences sharedPreferences = getSharedPreferences("SeekbarPrefs", Context.MODE_PRIVATE);
        // 读取之前保存的position
        int savedPosition = sharedPreferences.getInt("position", 2);  // 如果没有存储值，默认为 2

        et_email.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        et_email_verification_code.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        btn_send_code.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        et_password1.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        alert_validPassword1.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        et_password2.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        alert_validPassword2.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        alert_newPassword2.setTextSize(COMPLEX_UNIT_SP, getAlertSize(savedPosition));
        tv_returnLogin.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
        reset_password.setTextSize(COMPLEX_UNIT_SP, getCommonFontSize(savedPosition));
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
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上
        return false;
    }

    //隐藏软键盘并让editText失去焦点
    private void hideKeyboard(IBinder token) {
        et_email.clearFocus();
        et_email_verification_code.clearFocus();
        et_password1.clearFocus();
        et_password2.clearFocus();
        if (token != null) {
            //这里先获取InputMethodManager再调用他的方法来关闭软键盘
            //InputMethodManager就是一个管理窗口输入的manager
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        //验证密码合法性
        if (v.getId() == R.id.et_password4 && !hasFocus) {

            if (PasswordUtil.isPasswordValid(et_password1.getText().toString()))
                ll_alert_validPassword.setVisibility(View.GONE);
            else
                ll_alert_validPassword.setVisibility(View.VISIBLE);

        } else if (v.getId() == R.id.et_password5 && !hasFocus) {

            if (et_password1.getText().toString().equals(et_password2.getText().toString())) {
                alert_newPassword1.setVisibility(View.GONE);
                if (PasswordUtil.isPasswordValid(et_password2.getText().toString()))
                    ll_alert_validPassword2.setVisibility(View.GONE);
                else
                    ll_alert_validPassword2.setVisibility(View.VISIBLE);
            } else {
                alert_newPassword1.setVisibility(View.VISIBLE);
            }

        }
    }
}