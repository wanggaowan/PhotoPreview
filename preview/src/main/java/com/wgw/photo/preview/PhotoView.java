package com.wgw.photo.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.Scroller;

import com.github.chrisbanes.photoview.copy.OnScaleChangedListener;
import com.github.chrisbanes.photoview.copy.OnViewDragListener;
import com.github.chrisbanes.photoview.copy.PhotoViewAttacher;


/**
 * A zoomable ImageView. See {@link PhotoViewAttacher} for most of the details on how the zooming
 * is accomplished.<br>
 *
 * @author Created by wanggaowan on 11/19/20 11:23 PM
 */
class PhotoView extends com.github.chrisbanes.photoview.copy.PhotoView implements OnScaleChangedListener, OnViewDragListener {
    
    private static final int RESET_ANIM_TIME = 100;
    
    private final Scroller mScroller;
    private PhotoPreviewFragment mPhotoPreviewFragment;
    private ImageChangeListener mImageChangeListener;
    private DrawEndListener mDrawEndListener;
    private final ViewConfiguration mViewConfiguration;
    
    private boolean mDrawableChange = true;
    // 向下拖动触发
    private boolean mBottomDragging;
    private boolean mBgAnimStart;
    
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
        setOnScaleChangeListener(this);
        setOnViewDragListener(this);
        mScroller = new Scroller(context);
        mViewConfiguration = ViewConfiguration.get(context);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawableChange) {
            mDrawableChange = false;
            if (mDrawEndListener != null) {
                mDrawEndListener.onEnd();
            }
        }
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onFingerUp();
                break;
        }
        
        return super.dispatchTouchEvent(event);
    }
    
    private void onFingerUp() {
        mBottomDragging = false;
        if (getScale() > 1) {
            return;
        }
        
        // 这里恢复位置和透明度
        if (mIntAlpha != 255 && getScale() < 0.8) {
            mPhotoPreviewFragment.exit();
        } else {
            reset();
        }
    }
    
    private void reset() {
        mIntAlpha = 255;
        mBgAnimStart = true;
        mPhotoPreviewFragment.doViewBgAnim(Color.BLACK, RESET_ANIM_TIME, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBgAnimStart = false;
            }
        });
        
        mScroller.startScroll(
            getScrollX(),
            getScrollY(),
            -getScrollX(),
            -getScrollY(), RESET_ANIM_TIME
        );
        invalidate();
    }
    
    @Override
    public void onScaleChange(float scaleFactor, float focusX, float focusY) {
    
    }
    
    @Override
    public void onDrag(float dx, float dy) {
        boolean intercept = mBgAnimStart
            || Math.abs(dx) > Math.abs(dy)
            || Math.sqrt((dx * dx) + (dy * dy)) < mViewConfiguration.getScaledTouchSlop()
            || getScale() > 1
            || !hasVisibleDrawable();
        
        if (!mBottomDragging && intercept) {
            return;
        }
        
        if (!mBottomDragging) {
            // 执行拖拽操作，请求父类不要拦截请求
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        
        mBottomDragging = true;
        float scale = getScale();
        // 移动图像
        scrollBy(((int) -dx), ((int) -dy));
        float scrollY = getScrollY();
        if (scrollY >= 0) {
            scale = 1f;
            mIntAlpha = 255;
        } else {
            scale -= dy * 0.001f;
            mIntAlpha -= dy * 0.03;
        }
        
        if (scale > 1) {
            scale = 1f;
        } else if (scale < 0) {
            scale = 0f;
        }
        
        if (mIntAlpha < 200) {
            mIntAlpha = 200;
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
     * 是否存在可观察的图像
     */
    private boolean hasVisibleDrawable() {
        if (getDrawable() == null) {
            return false;
        }
        
        Drawable drawable = getDrawable();
        // 获得ImageView中Image的真实宽高，
        int dw = drawable.getBounds().width();
        int dh = drawable.getBounds().height();
        return dw > 0 && dh > 0;
    }
    
    @Override
    public float getAlpha() {
        return mIntAlpha;
    }
    
    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mDrawableChange = true;
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }
    
    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mDrawableChange = true;
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }
    
    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mDrawableChange = true;
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }
    
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mDrawableChange = true;
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }
    
    void setPhotoPreviewFragment(PhotoPreviewFragment photoPreviewFragment) {
        mPhotoPreviewFragment = photoPreviewFragment;
    }
    
    void setImageChangeListener(ImageChangeListener listener) {
        mImageChangeListener = listener;
    }
    
    void setDrawEndListener(DrawEndListener listener) {
        mDrawEndListener = listener;
    }
    
    /**
     * 设置的图片发生更改
     */
    interface ImageChangeListener {
        
        /**
         * 图片发生更改，但是此时并不一定绘制到界面
         */
        void onChange(Drawable drawable);
    }
    
    /**
     * 图片绘制完成
     */
    interface DrawEndListener {
        /**
         * 图片绘制完成,只在图片发生变更后的第一次绘制时回调
         */
        void onEnd();
    }
}
