package com.wgw.photopreview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;

import com.wgw.photo.preview.IndicatorType;
import com.wgw.photo.preview.PhotoPreview;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Created by 汪高皖 on 2019/3/11 0011 11:30
 */
public class TestFragment extends Fragment {
    private Context mContext;
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.activity_main2, null);
        initView(view.findViewById(R.id.rv));
        return view;
    }
    
    private void initView(final RecyclerView recyclerView) {
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 3);
        recyclerView.setLayoutManager(layoutManager);
        final PhotoAdapter adapter = new PhotoAdapter(Arrays.asList(MainActivity.picDataMore), ScaleType.CENTER);
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener((adapter1, view, position) ->
            PhotoPreview.with(this)
                .indicatorType(IndicatorType.TEXT)
                .selectIndicatorColor(0xffEE3E3E)
                .normalIndicatorColor(0xff3954A0)
                .delayShowProgressTime(200)
                .sources(Arrays.asList(MainActivity.picDataMore))
                .defaultShowPosition(position)
                .build()
                .show(position1 -> layoutManager.findViewByPosition(position1).findViewById(R.id.itemIv)));
    }
}
