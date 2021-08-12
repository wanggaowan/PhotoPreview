package com.wgw.photopreview;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
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
 * 缩略图界面非全屏，沉浸式状态栏，预览界面全屏
 */
public class Main5Activity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        // if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        //     // 需要设置这个才能设置状态栏和导航栏颜色，此时布局内容可绘制到状态栏之下
        //     window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // }
        // window.addFlags(LayoutParams.FLAG_FULLSCREEN);
    
        // 沉浸式处理
        // OPPO ANDROID P 之后的系统需要设置沉浸式配合异形屏适配才能将内容绘制到耳朵区域
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            // 防止系统栏隐藏时内容区域大小发生变化
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            // window全屏显示，但状态栏不会被隐藏，状态栏依然可见，内容可绘制到状态栏之下
            uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            // window全屏显示，但导航栏不会被隐藏，导航栏依然可见，内容可绘制到导航栏之下
            // uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            // if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            //     // 对于OPPO ANDROID P 之后的系统,一定需要清除此标志，否则异形屏无法绘制到耳朵区域下面
            //     // window.clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //     // 设置之后不会通过触摸屏幕调出导航栏
            //     // uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE; // 通过系统上滑或者下滑拉出导航栏后不会自动隐藏
            //     uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // 通过系统上滑或者下滑拉出导航栏后会自动隐藏
            // }

            // if (mShareData.config.fullScreen == null && fullScreen) {
            //     // 隐藏状态栏
            //     uiFlags |= View.INVISIBLE;
            // }

            window.getDecorView().setSystemUiVisibility(uiFlags);
            // window.getDecorView().setPadding(0, 0, 0, 0);
        }
        
        setContentView(R.layout.activity_main2);
        // ImmersionBar.with(this)
        //     .statusBarColor(R.color.colorPrimary)
        //     .navigationBarColor(R.color.white)
        //     .autoNavigationBarDarkModeEnable(true)
        //     .fitsSystemWindows(true)
        //     .init();
        
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
