package com.wgw.photo.preview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Scroller;

import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.OnViewDragListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

/**
 * A zoomable ImageView. See {@link PhotoViewAttacher} for most of the details on how the zooming
 * is accomplished.<br>
 *
 * add {@link #smoothResetPosition()} method
 *
 * @author Created by wanggaowan on 11/19/20 11:23 PM
 */
public class PhotoView extends com.github.chrisbanes.photoview.PhotoView implements OnScaleChangedListener, OnViewDragListener {
    
    private final Scroller mScroller;
    private PhotoPreviewFragment mPhotoPreviewFragment;
    
    // 透明度
    private int mIntAlpha = 255;
    
    public PhotoView(Context context) {
        this(context, null);
    }
    
    public PhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }
    
    public PhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        // 默认要设置最小缩放倍率为0，因为打开和关闭预览的时候，缩放倍率小于默认定义的1
        // 如果不设置最小比例比1小，则PhotoViewAttacher setScale将抛出异常
        setMinimumScale(0);
        setOnScaleChangeListener(this);
        setOnViewDragListener(this);
        mScroller = new Scroller(context);
        
    }
    
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean touchEvent = super.dispatchTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onFingerUp();
                break;
        }
        return touchEvent;
    }
    
    private void onFingerUp() {
        if (getScale() > 1) {
            return;
        }
        
        // 这里恢复位置和透明度
        if (mIntAlpha < 255 * 0.6) {
            mPhotoPreviewFragment.exit();
        } else {
            ValueAnimator va = ValueAnimator.ofFloat(getScale(), 1f);
            ValueAnimator bgVa = ValueAnimator.ofInt(mIntAlpha, 255);
            va.setDuration(200);
            bgVa.setDuration(200);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setScale((Float) animation.getAnimatedValue());
                }
            });
            
            bgVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mPhotoPreviewFragment.mRoot.getBackground().setAlpha((Integer) animation.getAnimatedValue());
                }
            });
            va.start();
            bgVa.start();
            smoothResetPosition();
        }
        
        mIntAlpha = 255;
    }
    
    @Override
    public void onScaleChange(float scaleFactor, float focusX, float focusY) {
        if (getScale() >= getAttacher().getMediumScale()) {
            setMinimumScale(1);
        } else {
            setMinimumScale(0);
        }
    }
    
    @Override
    public void onDrag(float dx, float dy) {
        if (getScale() > 1) {
            return;
        }
        
        float scale = getScale();
        // 移动图像
        scrollBy(((int) -dx), ((int) -dy));
        float scrollY = getScrollY();
        if (scrollY >= 0) {
            scale = 1f;
            mIntAlpha = 255;
        } else {
            scale -= dy * 0.001f;
            mIntAlpha -= dy * 0.25;
        }
        
        if (scale > 1) {
            scale = 1f;
        } else if (scale < 0) {
            scale = 0f;
        }
        
        if (mIntAlpha < 0) {
            mIntAlpha = 0;
        } else if (mIntAlpha > 255) {
            mIntAlpha = 255;
        }
        
        mPhotoPreviewFragment.mRoot.getBackground().setAlpha(mIntAlpha);
        if (scrollY < 0 && scale >= 0.6) {
            // 更改大小
            setScale(scale);
        }
    }
    
    /**
     * smooth reset view position to default position
     */
    public void smoothResetPosition() {
        mScroller.startScroll(
            getScrollX(),
            getScrollY(),
            -getScrollX(),
            -getScrollY(), 200
        );
        invalidate();
    }
    
    public void setPhotoPreviewFragment(PhotoPreviewFragment photoPreviewFragment) {
        mPhotoPreviewFragment = photoPreviewFragment;
    }
    
    @Override
    public float getAlpha() {
        return mIntAlpha;
    }
}
