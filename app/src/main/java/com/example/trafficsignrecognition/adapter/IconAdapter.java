package com.example.trafficsignrecognition.adapter;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.example.trafficsignrecognition.util.TextSizeUtil.getAlertSize;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.trafficsignrecognition.R;

import java.util.ArrayList;

public class IconAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Icon> mData;
    private final int mLayoutId;

    public IconAdapter(ArrayList<Icon> data, int layoutId, Context context) {
        this.context = context;
        this.mData = data;
        this.mLayoutId = layoutId;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Icon getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getIconNum();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(mLayoutId, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Icon icon = mData.get(position);
        bindView(holder, icon);

        return convertView;
    }

    protected void bindView(ViewHolder holder, Icon icon) {
        holder.setImageResource(icon.getIconId());
        holder.setText(icon.getIconName());
    }

    public static class ViewHolder {
        private final ImageView imgIcon;
        private final TextView txtIcon;
        private final Context context;

        public ViewHolder(View itemView) {
            imgIcon = itemView.findViewById(R.id.img_icon);
            txtIcon = itemView.findViewById(R.id.txt_icon);
            context = itemView.getContext();
        }

        public void setImageResource(int resId) {
            imgIcon.setImageResource(resId);
        }

        public void setText(String text) {
            txtIcon.setText(text);
            // 获取SharedPreferences
            SharedPreferences sharedPreferences = context.getSharedPreferences("SeekbarPrefs", Context.MODE_PRIVATE);
            // 读取之前保存的position
            int savedPosition = sharedPreferences.getInt("position", 2);  // 如果没有存储值，默认为 2
            txtIcon.setTextSize(COMPLEX_UNIT_SP,getAlertSize(savedPosition));
        }
    }
}

