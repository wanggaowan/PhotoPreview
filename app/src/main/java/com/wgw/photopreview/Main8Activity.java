package com.wgw.photopreview;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Glide;
import com.gyf.immersionbar.ImmersionBar;
import com.wgw.photo.preview.PhotoPreview;
import com.wgw.photo.preview.util.notch.CutOutMode;
import com.wgw.photo.preview.util.notch.NotchAdapterUtils;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 缩略图界面全屏，沉浸式，预览界面非全屏
 */
public class Main8Activity extends AppCompatActivity {
    
    private PhotoAdapter mAdapter;
    private PhotoAdapter mAdapter2;
    private ScaleType mScaleType = ScaleType.CENTER;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NotchAdapterUtils.adapter(getWindow(), CutOutMode.SHORT_EDGES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main8);
        
        ImmersionBar.with(this)
            .statusBarColor(R.color.colorPrimary)
            .navigationBarColor(R.color.white)
            .autoNavigationBarDarkModeEnable(true)
            .supportActionBar(true)
            .fitsSystemWindows(true)
            .init();
        
        RecyclerView rv = findViewById(R.id.rv);
        RecyclerView rv2 = findViewById(R.id.rv2);
        Button btScaleCenter = findViewById(R.id.bt_scale_center);
        Button btScaleFitCenter = findViewById(R.id.bt_scale_fit_center);
        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rv.setLayoutManager(layoutManager);
        mAdapter = new PhotoAdapter(Arrays.asList(MainActivity.picDataMore), mScaleType);
        rv.setAdapter(mAdapter);
        
        GridLayoutManager layoutManager2 = new GridLayoutManager(this, 3);
        rv2.setLayoutManager(layoutManager2);
        mAdapter2 = new PhotoAdapter(Arrays.asList(MainActivity.picDataMore), mScaleType);
        rv2.setAdapter(mAdapter2);
        
        mAdapter.setOnItemClickListener((adapter1, view, position) -> {
            PhotoPreview.with(Main8Activity.this)
                .imageLoader((position1, object, imageView1) -> {
                    Glide.with(Main8Activity.this)
                        .load(((String) object))
                        .into(imageView1);
                })
                .sources(Arrays.asList(MainActivity.picDataMore))
                .defaultShowPosition(position)
                .fullScreen(true)
                .build()
                .show(position1 -> layoutManager.findViewByPosition(position1).findViewById(R.id.itemIv));
        });
        
        mAdapter2.setOnItemClickListener((adapter1, view, position) -> {
            PhotoPreview.with(Main8Activity.this)
                .imageLoader((position1, object, imageView1) -> {
                    Glide.with(Main8Activity.this)
                        .load(((String) object))
                        .into(imageView1);
                })
                .sources(Arrays.asList(MainActivity.picDataMore))
                .defaultShowPosition(position)
                .fullScreen(true)
                .build()
                .show(layoutManager2 :: findViewByPosition);
        });
        
        btScaleCenter.setOnClickListener(v -> {
            if (mScaleType == ScaleType.CENTER) {
                return;
            }
            
            mScaleType = ScaleType.CENTER;
            mAdapter.setScaleType(mScaleType);
            mAdapter2.setScaleType(mScaleType);
        });
        
        btScaleFitCenter.setOnClickListener(v -> {
            if (mScaleType == ScaleType.FIT_CENTER) {
                return;
            }
            
            mScaleType = ScaleType.FIT_CENTER;
            mAdapter.setScaleType(mScaleType);
            mAdapter2.setScaleType(mScaleType);
        });
    }
}
