package com.wgw.photo.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 仅用于预加载
 *
 * @author Created by wanggaowan on 12/10/20 3:08 PM
 */
class PreloadImageView extends AppCompatImageView {
    
    public PreloadImageView(@NonNull Context context) {
        super(context);
    }
    
    public PreloadImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public PreloadImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    
    }
    
    @Override
    public void setImageResource(int resId) {
    
    }
    
    @Override
    public void setImageBitmap(Bitmap bm) {
    
    }
    
    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
    
    }
    
    @Override
    public void setImageURI(@Nullable Uri uri) {
    
    }
}
