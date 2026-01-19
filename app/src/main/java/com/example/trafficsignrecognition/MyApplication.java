package com.example.trafficsignrecognition;

import android.app.Application;
import android.util.Log;
import com.example.trafficsignrecognition.util.TextToSpeech;

public class MyApplication extends Application {

    private static MyApplication mApp;

    private TextToSpeech tts;

    public static MyApplication getInstance() {
        return mApp;
    }

    //程序创建的时候执行
    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        // 初始化TextToSpeech对象
        tts = new TextToSpeech(this);

    }

    public TextToSpeech getTts() {
        return tts;
    }


    // 程序终止的时候执行
    @Override
    public void onTerminate() {

        super.onTerminate();
        if(tts!=null){
            Log.d("TTS", "关闭TTS资源");
            tts.releaseModel();
        }
    }
}



