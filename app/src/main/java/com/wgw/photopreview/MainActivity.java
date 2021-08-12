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
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fcdn.duitang.com%2Fuploads%2Fitem%2F201303%2F29%2F20130329205806_kTTnv.thumb.700_0.jpeg&refer=http%3A%2F%2Fcdn.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631280979&t=ac3ffc327dcc2f074c7bff3198212cf2",
        "https://ss0.baidu.com/94o3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/b64543a98226cffc86abe943bc014a90f703eaba.jpg",
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fnimg.ws.126.net%2F%3Furl%3Dhttp%253A%252F%252Fdingyue.ws.126.net%252F2021%252F0319%252F30902a39j00qq7hp000sqc000u001hcm.jpg%26thumbnail%3D650x2147483647%26quality%3D80%26type%3Djpg&refer=http%3A%2F%2Fnimg.ws.126.net&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1618808541&t=3e477e8d3affd57b777802605e471f1e"
    };
    
    public static final String[] picDataMore = new String[]{
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fcdn.duitang.com%2Fuploads%2Fitem%2F201303%2F29%2F20130329205806_kTTnv.thumb.700_0.jpeg&refer=http%3A%2F%2Fcdn.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631280979&t=ac3ffc327dcc2f074c7bff3198212cf2",
        "https://t8.baidu.com/it/u=1484500186,1503043093&fm=79&app=86&size=h300&n=0&g=4n&f=jpeg?sec=1597826769&t=5aad7287dfe219994a7a59f78aca0006",
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201605%2F10%2F20160510001106_2YjCN.thumb.700_0.jpeg&refer=http%3A%2F%2Fb-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631281036&t=494931d2f88600781ccc98941c1c171e",
        "https://ss0.baidu.com/94o3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/b64543a98226cffc86abe943bc014a90f703eaba.jpg",
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fnimg.ws.126.net%2F%3Furl%3Dhttp%253A%252F%252Fdingyue.ws.126.net%252F2021%252F0319%252F30902a39j00qq7hp000sqc000u001hcm.jpg%26thumbnail%3D650x2147483647%26quality%3D80%26type%3Djpg&refer=http%3A%2F%2Fnimg.ws.126.net&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1618808541&t=3e477e8d3affd57b777802605e471f1e",
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fnimg.ws.126.net%2F%3Furl%3Dhttp%253A%252F%252Fdingyue.ws.126.net%252F2021%252F0812%252Ff2ed8a22j00qxp4oy004yc000dc01u1c.jpg%26thumbnail%3D650x2147483647%26quality%3D80%26type%3Djpg&refer=http%3A%2F%2Fnimg.ws.126.net&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631337174&t=60592ca90695c685514a5c60272aec57",
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F48124ed00ae163228d4e65acf0d54c5cc5a2f31a2e142-PaXGuD_fw658&refer=http%3A%2F%2Fhbimg.b0.upaiyun.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631281100&t=b2226ca4bcde9a4fffc5bc4e2300d1ac",
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2F5b0988e595225.cdn.sohucs.com%2Fimages%2F20190917%2F2603e4d3e9f54ec08cd22d9d9cb6b539.JPG&refer=http%3A%2F%2F5b0988e595225.cdn.sohucs.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1618808998&t=358c52bfaa044bf5b6ca51935de10c42",
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F9f569629c4dec5ed1b603982058c6853607b1f0af685e-PcenmQ_fw658&refer=http%3A%2F%2Fhbimg.b0.upaiyun.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631281144&t=e56fa317e4de55b22bcb49f94cc042a7",
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
    
    public void goMain8Activity(View view) {
        startActivity(new Intent(this, Main8Activity.class));
    }
    
    public void goMain9Activity(View view) {
        startActivity(new Intent(this, Main9Activity.class));
    }
}
