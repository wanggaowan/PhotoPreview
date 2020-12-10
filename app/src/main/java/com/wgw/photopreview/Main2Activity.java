package com.wgw.photopreview;

import android.os.Bundle;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.wgw.photo.preview.IndicatorType;
import com.wgw.photo.preview.PhotoPreview;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Main2Activity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    
        final RecyclerView recyclerView = findViewById(R.id.rv);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(layoutManager);
        final PhotoAdapter adapter = new PhotoAdapter(Arrays.asList(MainActivity.picDataMore), ScaleType.CENTER);
        recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            PhotoPreview.with(Main2Activity.this)
                .indicatorType(IndicatorType.DOT)
                .selectIndicatorColor(0xffEE3E3E)
                .normalIndicatorColor(0xff3954A0)
                .delayShowProgressTime(200)
                .imageLoader((position1, url, imageView1) ->
                    Glide.with(Main2Activity.this)
                        .load(((String) url))
                        .override(Target.SIZE_ORIGINAL)
                        .into(imageView1))
                .sources(Arrays.asList(MainActivity.picDataMore))
                .defaultShowPosition(position)
                .build()
                .show(position1 -> layoutManager.findViewByPosition(position1).findViewById(R.id.itemIv));
        });
    }
}
