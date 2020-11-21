package com.wgw.photopreview;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wgw.photo.preview.PhotoPreview;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String[] picData = new String[]{
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232069076&di=9adc33f7da408f2c60ec5a40d19f8732&imgtype=0&src=http%3A%2F%2Ft9.baidu.com%2Fit%2Fu%3D583874135%2C70653437%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D3607%26h%3D2408",
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232103560&di=d3834a4115d19c23b163b5c25dc5bed2&imgtype=0&src=http%3A%2F%2Fimage.cilacila.com%2Fuploads%2F20190108%2F10%2F1546915052-eADxLaZHCc.jpeg",
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232129112&di=9f2110e2e9e50aa1d29e6906c3182df7&imgtype=0&src=http%3A%2F%2Fimg.wezhan.cn%2Fcontent%2Fsitefiles%2F63673%2Fimages%2F6885365_3.jpeg"
    };
    
    public static final String[] picDataMore = new String[]{
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232022348&di=b8feaf86b54bb1456e6ca12a2087b2fa&imgtype=0&src=http%3A%2F%2Ft8.baidu.com%2Fit%2Fu%3D2247852322%2C986532796%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D1280%26h%3D853",
        "https://t8.baidu.com/it/u=1484500186,1503043093&fm=79&app=86&size=h300&n=0&g=4n&f=jpeg?sec=1597826769&t=5aad7287dfe219994a7a59f78aca0006",
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232069076&di=9adc33f7da408f2c60ec5a40d19f8732&imgtype=0&src=http%3A%2F%2Ft9.baidu.com%2Fit%2Fu%3D583874135%2C70653437%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D3607%26h%3D2408",
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232103560&di=d3834a4115d19c23b163b5c25dc5bed2&imgtype=0&src=http%3A%2F%2Fimage.cilacila.com%2Fuploads%2F20190108%2F10%2F1546915052-eADxLaZHCc.jpeg",
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232129112&di=9f2110e2e9e50aa1d29e6906c3182df7&imgtype=0&src=http%3A%2F%2Fimg.wezhan.cn%2Fcontent%2Fsitefiles%2F63673%2Fimages%2F6885365_3.jpeg"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 设置全局图片加载器
        PhotoPreview.setGlobalImageLoader((position, source, imageView) ->
            Glide.with(imageView.getContext())
                .load((String) source)
                .into(imageView));
    }
    
    
    public void goPreview(View view) {
        startActivity(new Intent(this, Main2Activity.class));
    }
    
    public void goMain3Activity(View view) {
        startActivity(new Intent(this, Main3Activity.class));
    }
    
    public void showDialogPreview(View view) {
        @SuppressLint("InflateParams")
        View root = LayoutInflater.from(this).inflate(R.layout.view_dialog, null);
        final ImageView iv1 = root.findViewById(R.id.iv1);
        final ImageView iv2 = root.findViewById(R.id.iv2);
        final ImageView iv3 = root.findViewById(R.id.iv3);
        loadView(picData[0], iv1);
        loadView(picData[1], iv2);
        loadView(picData[2], iv3);
        
        iv1.setOnClickListener(v ->
            PhotoPreview.with(MainActivity.this)
                .sources(picData[0])
                .build()
                .show(iv1));
        
        iv2.setOnClickListener(v ->
            PhotoPreview.with(MainActivity.this)
                .delayShowProgressTime(200)
                .sources(picData[1])
                .build()
                .show());
        
        iv3.setOnClickListener(v ->
            PhotoPreview.with(MainActivity.this)
                .sources(picData[2])
                .onDismissListener(() -> Toast.makeText(MainActivity.this, "界面关闭", Toast.LENGTH_SHORT).show())
                .build()
                .show(iv3));
        
        Dialog dialog = new Dialog(this);
        dialog.setContentView(root);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            Point point = new Point();
            if (Build.VERSION.SDK_INT >= 17) {
                defaultDisplay.getRealSize(point);
            } else {
                defaultDisplay.getSize(point);
            }
            
            WindowManager.LayoutParams wl = window.getAttributes();
            wl.width = (int) (point.x * 0.8f);
            wl.height = (int) (point.y * 0.8f);
            window.setAttributes(wl);
        }
        dialog.show();
    }
    
    private void loadView(String url, ImageView imageView) {
        Glide.with(this)
            .load(url)
            .into(imageView);
    }
    
    public void goMain4Activity(View view) {
        startActivity(new Intent(this, Main4Activity.class));
        PhotoPreview.with(this).build().dismiss();
    }
    
    public void goMain5Activity(View view) {
        startActivity(new Intent(this, Main5Activity.class));
    }
    
    public void goMain6Activity(View view) {
        startActivity(new Intent(this, Main6Activity.class));
    }
    
    public void goMain7Activity(View view) {
        startActivity(new Intent(this, Main7Activity.class));
    }
}
