package com.example.trafficsignrecognition.ui.gallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.trafficsignrecognition.R;
import com.example.trafficsignrecognition.adapter.IconAdapter;
import com.example.trafficsignrecognition.databinding.FragmentGalleryBinding;
import com.example.trafficsignrecognition.adapter.Icon;
import com.example.trafficsignrecognition.util.ConstantUtil;
import com.example.trafficsignrecognition.util.ImageDetailDialog;
import com.example.trafficsignrecognition.util.SharedViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private ArrayList<Icon> mDataProhibition = null;
    private ArrayList<Icon> mDataInstruction = null;
    private ArrayList<Icon> mDataWarning = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        GridView gridviewProhibition = binding.gridviewProhibition;
        GridView gridviewInstruction = binding.gridviewInstruction;
        GridView gridviewWarning = binding.gridviewWarning;

        mDataProhibition = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            mDataProhibition.add(new Icon(ConstantUtil.id[i], ConstantUtil.name[i],i));
        }
        for (int i = 13; i < 18; i++) {
            mDataProhibition.add(new Icon(ConstantUtil.id[i], ConstantUtil.name[i],i));
        }
        mDataProhibition.add(new Icon(ConstantUtil.id[32], ConstantUtil.name[32],32));
        mDataProhibition.add(new Icon(ConstantUtil.id[41], ConstantUtil.name[41],41));
        mDataProhibition.add(new Icon(ConstantUtil.id[42], ConstantUtil.name[42],42));

        mDataInstruction = new ArrayList<>();
        for (int i = 33; i < 41; i++) {
            mDataInstruction.add(new Icon(ConstantUtil.id[i], ConstantUtil.name[i],i));
        }
        mDataInstruction.add(new Icon(ConstantUtil.id[12], ConstantUtil.name[12],12));

        mDataWarning = new ArrayList<>();
        for (int i = 18; i < 32; i++) {
            mDataWarning.add(new Icon(ConstantUtil.id[i], ConstantUtil.name[i],i));
        }


        BaseAdapter mAdapterProhibition = new IconAdapter(mDataProhibition, R.layout.item_grid_icon, getContext()) {
            @Override
            public void bindView(ViewHolder holder, Icon icon) {
                holder.setImageResource(icon.getIconId());
                holder.setText(icon.getIconName());
            }
        };
        gridviewProhibition.setAdapter(mAdapterProhibition);
        gridviewProhibition.setOnItemClickListener((parent, view, position, id) -> {
            //在自定义 Adapter 的情况下，id 通常和 position 相同，因为 BaseAdapter 默认实现 getItemId(int position) 方法时会返回 position。
            // 如果需要为每一项指定特定的 ID，可以在 Adapter 中重写 getItemId() 方法。
            // 显示 Dialog
            new ImageDetailDialog(getContext(), ConstantUtil.id[(int) id], ConstantUtil.description[(int) id]).show();
        });

        BaseAdapter mAdapterInstruction = new IconAdapter(mDataInstruction, R.layout.item_grid_icon, getContext()) {
            @Override
            public void bindView(ViewHolder holder, Icon icon) {
                holder.setImageResource(icon.getIconId());
                holder.setText(icon.getIconName());
            }
        };
        gridviewInstruction.setAdapter(mAdapterInstruction);
        gridviewInstruction.setOnItemClickListener((parent, view, position, id) -> {
            // 显示 Dialog
            new ImageDetailDialog(getContext(), ConstantUtil.id[(int) id], ConstantUtil.description[(int) id]).show();
        });


        BaseAdapter mAdapterWarning = new IconAdapter(mDataWarning, R.layout.item_grid_icon, getContext()) {
            @Override
            public void bindView(ViewHolder holder, Icon icon) {
                holder.setImageResource(icon.getIconId());
                holder.setText(icon.getIconName());
            }
        };
        gridviewWarning.setAdapter(mAdapterWarning);
        gridviewWarning.setOnItemClickListener((parent, view, position, id) -> {
            // 显示 Dialog
            new ImageDetailDialog(getContext(), ConstantUtil.id[(int) id], ConstantUtil.description[(int) id]).show();
        });

        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        SharedViewModel themeViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);
        Toolbar toolbar = themeViewModel.getToolbar();
        themeViewModel.getThemeColor().observe(getViewLifecycleOwner(), newColor -> toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), newColor)));



        // 调整GridView高度
        setGridViewHeightBasedOnChildren(gridviewProhibition, 4);
        setGridViewHeightBasedOnChildren(gridviewInstruction, 4);
        setGridViewHeightBasedOnChildren(gridviewWarning, 4);


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        int themeColor = sharedPreferences.getInt("themeColor", R.color.green);  // 如果没有存储值，默认为绿色
        SharedViewModel themeViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        themeViewModel.setThemeColor(themeColor);
        Toolbar toolbar = themeViewModel.getToolbar();
        themeViewModel.getThemeColor().observe(getViewLifecycleOwner(), newColor -> toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), newColor)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public static void setGridViewHeightBasedOnChildren(GridView gridView, int numColumns) {
        BaseAdapter baseAdapter = (BaseAdapter) gridView.getAdapter();
        if (baseAdapter == null) {
            // Adapter is not set yet, return early
            return;
        }

        int totalHeight = 0;
        int items = baseAdapter.getCount();
        int rows = (int) Math.ceil(items / (double) numColumns);

        for (int i = 0; i < rows; i++) {
            View listItem = baseAdapter.getView(i, null, gridView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight + (gridView.getVerticalSpacing() * (rows - 1));
        gridView.setLayoutParams(params);
        gridView.requestLayout();
    }

}