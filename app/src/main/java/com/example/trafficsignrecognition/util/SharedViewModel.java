package com.example.trafficsignrecognition.util;

import android.annotation.SuppressLint;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

@SuppressLint("StaticFieldLeak")
public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedColor = new MutableLiveData<>();

    private Toolbar toolbar;


    private final MutableLiveData<Boolean> switchBroadcast = new MutableLiveData<>();

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public LiveData<Integer> getThemeColor() {
        return selectedColor;
    }

    public void setThemeColor(int color) {
        selectedColor.setValue(color);
    }


    public LiveData<Boolean> getBroadcast() {
        return switchBroadcast;
    }

    public void setBroadcast(Boolean broadcast) {
        switchBroadcast.setValue(broadcast);
    }

}
