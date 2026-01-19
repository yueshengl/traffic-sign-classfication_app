package com.example.trafficsignrecognition.ui.slideshow;


import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trafficsignrecognition.util.UsernameUtil;

public class SlideshowViewModel extends ViewModel {

    private final MutableLiveData<String> imagePath;

    private final MutableLiveData<String> username;
    private final MutableLiveData<String> email;

    public SlideshowViewModel(SharedPreferences preferences) {
        // 初始化 LiveData 对象
        imagePath = new MutableLiveData<>();
        username = new MutableLiveData<>();
        email = new MutableLiveData<>();
        // 从 SharedPreferences 获取值并设置
        String savedEmail = preferences.getString("email", "");
        String savedUsername = preferences.getString("username", "用户9999zz");
        String savedImagePath = preferences.getString("imagePath", "null");

        email.setValue(savedEmail);
        username.setValue(savedUsername);
        imagePath.setValue(savedImagePath);

    }

    public LiveData<String> getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath.setValue(imagePath);
    }

    public LiveData<String> getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public LiveData<String> getEmail(){return email;}
}