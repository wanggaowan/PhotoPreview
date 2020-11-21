package com.wgw.photopreview;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

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
public class Main7Activity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NotchAdapterUtils.adapter(getWindow(), CutOutMode.SHORT_EDGES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ImmersionBar.with(this)
            .statusBarColor(R.color.colorPrimary)
            .navigationBarColor(R.color.white)
            .autoNavigationBarDarkModeEnable(true)
            .fitsSystemWindows(true)
            .init();
        
        final RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        final PhotoAdapter adapter = new PhotoAdapter(Arrays.asList(MainActivity.picDataMore));
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            final ImageView imageView = view.findViewById(R.id.itemIv);
            PhotoPreview.with(Main7Activity.this)
                .imageLoader((position1, object, imageView1) -> {
                    Glide.with(Main7Activity.this)
                        .load(((String) object))
                        .placeholder(position == position1 ? imageView.getDrawable() : null)
                        .into(imageView1);
                })
                .onDismissListener(() -> Toast.makeText(Main7Activity.this, "界面关闭", Toast.LENGTH_SHORT).show())
                .sources(Arrays.asList(MainActivity.picDataMore))
                .defaultShowPosition(position)
                .fullScreen(false)
                .build()
                .show(recyclerView);
        });
    }
}
