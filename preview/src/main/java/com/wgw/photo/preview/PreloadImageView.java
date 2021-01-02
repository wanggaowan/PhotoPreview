package com.wgw.photo.preview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 仅用于辅助加载，获取图片
 *
 * @author Created by wanggaowan on 12/10/20 3:08 PM
 */
@RestrictTo(Scope.LIBRARY)
public class PreloadImageView extends AppCompatImageView {
    
    private ImageView mActualView;
    
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
    public void setImageDrawable(@Nullable Drawable drawable) {
        if (mActualView != null) {
            mActualView.setImageDrawable(drawable);
        }
    }
    
    @Override
    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
    }
    
    void setActualView(ImageView imageView) {
        mActualView = imageView;
    }
}
