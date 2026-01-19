package com.example.trafficsignrecognition.util;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getCommonFontSize;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.trafficsignrecognition.R;

public class ImageDetailDialog extends Dialog {

    public ImageDetailDialog(Context context, int imageResId, String description) {
        super(context);
        this.imageResId = imageResId;
        this.description = description;
    }

    private final int imageResId;
    private final String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_image_detail);
        // 获取SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SeekbarPrefs", Context.MODE_PRIVATE);
        // 读取之前保存的position
        int savedPosition = sharedPreferences.getInt("position", 2);  // 如果没有存储值，默认为 2

        ImageView imageView = findViewById(R.id.image_view);
        TextView textView = findViewById(R.id.text_view);

        imageView.setImageResource(imageResId);
        textView.setText(description);
        textView.setTextSize(COMPLEX_UNIT_SP,getCommonFontSize(savedPosition));
    }
}
