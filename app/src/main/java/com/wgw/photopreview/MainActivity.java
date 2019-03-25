package com.wgw.photopreview;

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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wgw.photo.preview.PhotoPreview;
import com.wgw.photo.preview.interfaces.ImageLoader;

public class MainActivity extends AppCompatActivity {
    final String[] picData = new String[]{
        "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-704146.jpg",
        "http://pic1.nipic.com/2008-12-30/200812308231244_2.jpg",
        "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1859156282,3890550903&fm=27&gp=0.jpg",
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
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(root)
            .setCancelable(true)
            .create();
        dialog.show();
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
            wl.width = point.x;
            wl.height = (int) (point.y * 0.8f);
            window.setAttributes(wl);
        }
    }
    
    private void loadView(String url, ImageView imageView) {
        Glide.with(this)
            .load(url)
            .into(imageView);
    }
}
