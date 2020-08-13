package com.wgw.photopreview;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wgw.photo.preview.PhotoPreview;
import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnDismissListener;

import java.util.Arrays;

public class Main2Activity extends AppCompatActivity {
    private int clickPosition;
    private Drawable clickPositionDrawable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        final String[] picData = new String[]{
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232022348&di=b8feaf86b54bb1456e6ca12a2087b2fa&imgtype=0&src=http%3A%2F%2Ft8.baidu.com%2Fit%2Fu%3D2247852322%2C986532796%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D1280%26h%3D853",
            "https://t8.baidu.com/it/u=1484500186,1503043093&fm=79&app=86&size=h300&n=0&g=4n&f=jpeg?sec=1597826769&t=5aad7287dfe219994a7a59f78aca0006",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232069076&di=9adc33f7da408f2c60ec5a40d19f8732&imgtype=0&src=http%3A%2F%2Ft9.baidu.com%2Fit%2Fu%3D583874135%2C70653437%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D3607%26h%3D2408",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232103560&di=d3834a4115d19c23b163b5c25dc5bed2&imgtype=0&src=http%3A%2F%2Fimage.cilacila.com%2Fuploads%2F20190108%2F10%2F1546915052-eADxLaZHCc.jpeg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597232129112&di=9f2110e2e9e50aa1d29e6906c3182df7&imgtype=0&src=http%3A%2F%2Fimg.wezhan.cn%2Fcontent%2Fsitefiles%2F63673%2Fimages%2F6885365_3.jpeg"
        };
        
        final RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        final PhotoAdapter adapter = new PhotoAdapter(Arrays.asList(picData));
        recyclerView.setAdapter(adapter);
        
        final PhotoPreview photoPreview = new PhotoPreview(this, new ImageLoader() {
            @Override
            public void onLoadImage(int position, Object object, ImageView imageView) {
                Glide.with(Main2Activity.this)
                    .load(((String) object))
                    .placeholder(position == clickPosition ? clickPositionDrawable : null)
                    .into(imageView);
            }
        });
        photoPreview.setDelayShowProgressTime(200);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            photoPreview.setProgressColor(Color.WHITE);
        }
        
        photoPreview.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                Toast.makeText(Main2Activity.this, "界面关闭", Toast.LENGTH_SHORT).show();
            }
        });
        
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ImageView imageView = view.findViewById(R.id.itemIv);
                clickPositionDrawable = imageView.getDrawable();
                clickPosition = position;
                photoPreview.show(recyclerView, position, Arrays.<Object>asList(picData));
            }
        });
    }
}
