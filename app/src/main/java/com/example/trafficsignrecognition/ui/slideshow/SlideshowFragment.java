package com.example.trafficsignrecognition.ui.slideshow;

import static android.content.Context.MODE_PRIVATE;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getCommonFontSize;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.trafficsignrecognition.R;
import com.example.trafficsignrecognition.activity.UpdatePasswordActivity;
import com.example.trafficsignrecognition.databinding.FragmentSlideshowBinding;
import com.example.trafficsignrecognition.event.ImagePathChangedEvent;
import com.example.trafficsignrecognition.event.UsernameChangedEvent;
import com.example.trafficsignrecognition.util.ImageChooseUtil;
import com.example.trafficsignrecognition.util.SharedViewModel;
import com.example.trafficsignrecognition.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    //头像
    private CircleImageView ivHeaderImage;
    private TextView tvHeaderImage;
    private EditText etUsername;
    private TextView tvEmail;
    private Button btnSave;
    private SlideshowViewModel slideshowViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);\
        // 获取 SharedPreferences 对象
        SharedPreferences preferences = Objects.requireNonNull(getContext()).getSharedPreferences("login", MODE_PRIVATE);
        slideshowViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new SlideshowViewModel(preferences);
            }
        }).get(SlideshowViewModel.class);

        // 注册EventBus
        EventBus.getDefault().register(this);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ivHeaderImage = binding.ivHeaderImage;
        tvHeaderImage = binding.tvHeaderImage;
        etUsername = binding.etUsername;
        tvEmail = binding.tvEmail2;
        btnSave = binding.btnSave;
        LinearLayout llUpdatePassword = binding.llUpdatePassword;
        //设置头像
        slideshowViewModel.getImagePath().observe(getViewLifecycleOwner(), imagePath -> {
            if (!imagePath.equals("null")) {
                // 使用Glide加载图片
                Glide.with(this)
                        .load(imagePath)
                        .into(ivHeaderImage);
            } else {
                // 使用Glide加载图片
                Glide.with(this)
                        .load(R.drawable.headshot)
                        .into(ivHeaderImage);
            }
        });
        //为头像设置点击事件
        tvHeaderImage.setOnClickListener(view -> ImageChooseUtil.showPhotoOptionsDialog(this.getActivity()));
        //设置etUsername
        slideshowViewModel.getUsername().observe(getViewLifecycleOwner(), etUsername::setText);
        //设置tvPhoneNumber
        slideshowViewModel.getEmail().observe(getViewLifecycleOwner(), tvEmail::setText);
        //为llUpdatePassword设置监听事件
        llUpdatePassword.setOnClickListener(view -> {
            //跳转至UpdatePasswordActivity
            Intent intent = new Intent(this.getActivity(), UpdatePasswordActivity.class);
            startActivity(intent);
        });
        //为btnSave设置监听事件,实际上只能更新用户名
        btnSave.setOnClickListener(view -> {
            String newUsername = etUsername.getText().toString();
            // 获取 SharedPreferences.Editor 对象
            SharedPreferences.Editor editor = preferences.edit();
            // 修改 imagePath 的值
            editor.putString("username", newUsername);
            // 提交更改
            editor.apply();
            //通过事件总线更改用户名
            slideshowViewModel.setUsername(newUsername);
            EventBus.getDefault().post(new UsernameChangedEvent(newUsername));
            ToastUtil.show(this.getActivity(), "保存成功");

        });
        handleTextSize();
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        SharedViewModel themeViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);

        themeViewModel.getThemeColor().observe(getViewLifecycleOwner(), newColor -> {
            int color = ContextCompat.getColor(getContext(), newColor);  // 替换为你想要的颜色
            ViewCompat.setBackgroundTintList(btnSave, ColorStateList.valueOf(color));
        });
        return root;
    }

    private void handleTextSize() {
        // 获取SharedPreferences
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("SeekbarPrefs", MODE_PRIVATE);
        // 读取之前保存的position
        int savedPosition = sharedPreferences.getInt("position", 2);  // 如果没有存储值，默认为 2

        tvHeaderImage.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        binding.tvUsername.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        etUsername.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        binding.tvEmail.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        tvEmail.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        binding.tvUpdatePassword2.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
        btnSave.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        SharedViewModel themeViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);
        themeViewModel.getThemeColor().observe(getViewLifecycleOwner(), newColor -> {
            int color = ContextCompat.getColor(getContext(), newColor);  // 替换为你想要的颜色
            ViewCompat.setBackgroundTintList(btnSave, ColorStateList.valueOf(color));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImagePathChangedEvent(ImagePathChangedEvent event) {
        //更改图片路径
        slideshowViewModel.setImagePath(event.getImagePath());
    }

}