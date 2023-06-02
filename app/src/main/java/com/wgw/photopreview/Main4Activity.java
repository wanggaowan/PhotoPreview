package com.wgw.photopreview;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wgw.photo.preview.PhotoPreview;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 缩略图界面非全屏，非沉浸式状态栏，预览界面全屏
 */
public class Main4Activity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//
        }
        setContentView(R.layout.activity_main2);
        final RecyclerView recyclerView = findViewById(R.id.rv);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        final PhotoAdapter adapter = new PhotoAdapter(Arrays.asList(MainActivity.picDataMore), ScaleType.CENTER_INSIDE);
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            final ImageView imageView = view.findViewById(R.id.itemIv);
            PhotoPreview.with(Main4Activity.this)
                .imageLoader((position1, object, imageView1) -> {
                    Glide.with(Main4Activity.this)
                        .load(((String) object))
                        .placeholder(position == position1 ? imageView.getDrawable() : null)
                        .into(imageView1);
                })
                .onDismissListener(() -> Toast.makeText(Main4Activity.this, "界面关闭", Toast.LENGTH_SHORT).show())
                .sources(Arrays.asList(MainActivity.picDataMore))
                .defaultShowPosition(position)
                .fullScreen(true)
                .build()
                .show(position1 -> layoutManager.findViewByPosition(position1).findViewById(R.id.itemIv));
        });
    }
}
