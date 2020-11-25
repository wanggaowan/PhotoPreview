package com.wgw.photopreview;

import android.os.Bundle;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Glide;
import com.wgw.photo.preview.IndicatorType;
import com.wgw.photo.preview.PhotoPreview;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Main9Activity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        
        final RecyclerView recyclerView = findViewById(R.id.rv);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        final PhotoAdapter adapter = new PhotoAdapter(Arrays.asList(MainActivity.picDataMore), ScaleType.CENTER_CROP);
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            PhotoPreview.with(Main9Activity.this)
                .indicatorType(IndicatorType.DOT)
                .selectIndicatorColor(0xffEE3E3E)
                .normalIndicatorColor(0xff3954A0)
                .delayShowProgressTime(200)
                .imageLoader((position1, url, imageView1) ->
                    Glide.with(Main9Activity.this)
                        .load(((String) url))
                        .into(imageView1))
                .sources(Arrays.asList(MainActivity.picDataMore))
                .defaultShowPosition(position)
                .animDuration(0L)
                .build()
                .show(position1 -> layoutManager.findViewByPosition(position1).findViewById(R.id.itemIv));
        });
    }
}
