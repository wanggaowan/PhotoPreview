package com.wgw.photopreview;

import android.os.Bundle;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Glide;
import com.gyf.immersionbar.ImmersionBar;
import com.wgw.photo.preview.PhotoPreview;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 缩略图界面非全屏，沉浸式状态栏，预览界面全屏
 */
public class Main5Activity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ImmersionBar.with(this)
            .statusBarColor(R.color.colorPrimary)
            .navigationBarColor(R.color.white)
            .autoNavigationBarDarkModeEnable(true)
            .fitsSystemWindows(true)
            .init();
        
        final RecyclerView recyclerView = findViewById(R.id.rv);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        final PhotoAdapter adapter = new PhotoAdapter(Arrays.asList(MainActivity.picDataMore), ScaleType.FIT_CENTER);
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            PhotoPreview.with(Main5Activity.this)
                .imageLoader((position1, object, imageView1) -> {
                    Glide.with(Main5Activity.this)
                        .load(((String) object))
                        .into(imageView1);
                })
                .sources(Arrays.asList(MainActivity.picDataMore))
                .defaultShowPosition(position)
                .fullScreen(true)
                .build()
                .show(position1 -> layoutManager.findViewByPosition(position1).findViewById(R.id.itemIv));
        });
    }
}
