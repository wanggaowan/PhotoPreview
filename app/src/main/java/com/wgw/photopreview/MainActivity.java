package com.wgw.photopreview;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wgw.photo.preview.PhotoPreview;
import com.wgw.photo.preview.interfaces.ImageLoader;

public class MainActivity extends AppCompatActivity {
    final String[] picData = new String[]{
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232069076&di=9adc33f7da408f2c60ec5a40d19f8732&imgtype=0&src=http%3A%2F%2Ft9.baidu.com%2Fit%2Fu%3D583874135%2C70653437%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D3607%26h%3D2408",
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232103560&di=d3834a4115d19c23b163b5c25dc5bed2&imgtype=0&src=http%3A%2F%2Fimage.cilacila.com%2Fuploads%2F20190108%2F10%2F1546915052-eADxLaZHCc.jpeg",
        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232129112&di=9f2110e2e9e50aa1d29e6906c3182df7&imgtype=0&src=http%3A%2F%2Fimg.wezhan.cn%2Fcontent%2Fsitefiles%2F63673%2Fimages%2F6885365_3.jpeg"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void goPreview(View view) {
        startActivity(new Intent(this, Main2Activity.class));
    }
    
    public void goMain3Activity(View view) {
        startActivity(new Intent(this, Main3Activity.class));
    }
    
    public void showDialogPreview(View view) {
        View root = LayoutInflater.from(this).inflate(R.layout.view_dialog, null);
        final ImageView iv1 = root.findViewById(R.id.iv1);
        final ImageView iv2 = root.findViewById(R.id.iv2);
        final ImageView iv3 = root.findViewById(R.id.iv3);
        loadView(picData[0], iv1);
        loadView(picData[1], iv2);
        loadView(picData[2], iv3);
        
        final PhotoPreview photoPreview = new PhotoPreview(this, new ImageLoader() {
            @Override
            public void onLoadImage(int position, Object object, ImageView imageView) {
                loadView((String) object, imageView);
            }
        });
        
        photoPreview.setDelayShowProgressTime(200);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            photoPreview.setProgressColor(Color.WHITE);
        }
        
        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoPreview.show(iv1, picData[0]);
            }
        });
        
        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoPreview.show(iv2, picData[1]);
            }
        });
        
        iv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoPreview.show(iv3, picData[2]);
            }
        });
        
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
}
