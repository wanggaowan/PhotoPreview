package com.wgw.photo.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.Scroller;

import com.github.chrisbanes.photoview.custom.OnScaleChangedListener;
import com.github.chrisbanes.photoview.custom.OnViewDragListener;
import com.github.chrisbanes.photoview.custom.PhotoViewAttacher;


/**
 * A zoomable ImageView. See {@link PhotoViewAttacher} for most of the details on how the zooming
 * is accomplished.<br>
 *
 * @author Created by wanggaowan on 11/19/20 11:23 PM
 */
class PhotoView extends com.github.chrisbanes.photoview.custom.PhotoView implements OnScaleChangedListener, OnViewDragListener {
    
    private static final int RESET_ANIM_TIME = 100;
    
    private final Scroller mScroller;
    
    // 是否是预览的第一个View
    private boolean mStartView = false;
    // 是否是预览的最后一个View
    private boolean mEndView = false;
    private PhotoPreviewHelper mHelper;
    private ImageChangeListener mImageChangeListener;
    private final ViewConfiguration mViewConfiguration;
    
    // 当前是否正在拖拽
    private boolean mDragging;
    private boolean mBgAnimStart;
    
    // 透明度
    private int mIntAlpha = 255;
    // 记录缩放后垂直方向边界判定值
    private int mScaleVerticalScrollEdge = PhotoViewAttacher.VERTICAL_EDGE_INSIDE;
    // 记录缩放后水平方向边界判定值
    private int mScaleHorizontalScrollEdge = PhotoViewAttacher.HORIZONTAL_EDGE_INSIDE;
    private OnScaleChangedListener mOnScaleChangedListener;
    
    public PhotoView(Context context) {
        this(context, null);
    }
    
    public PhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }
    
    public PhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        super.setOnScaleChangeListener(this);
        setOnViewDragListener(this);
        mScroller = new Scroller(context);
        mViewConfiguration = ViewConfiguration.get(context);
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
        mDragging = false;
        if (getScale() > 1) {
            if (Math.abs(getScrollX()) > 0 || Math.abs(getScrollY()) > 0) {
                reset();
            }
            return;
        }
        
        // 这里恢复位置和透明度
        if (mIntAlpha != 255 && getScale() < 0.8) {
            mHelper.exit();
        } else {
            reset();
        }
    }
    
    private void reset() {
        mIntAlpha = 255;
        mBgAnimStart = true;
        mHelper.doViewBgAnim(Color.BLACK, RESET_ANIM_TIME, new AnimatorListenerAdapter() {
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
    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangedListener) {
        mOnScaleChangedListener = onScaleChangedListener;
    }
    
    @Override
    public void onScaleChange(float scaleFactor, float focusX, float focusY) {
        mScaleVerticalScrollEdge = attacher.getVerticalScrollEdge();
        mScaleHorizontalScrollEdge = attacher.getHorizontalScrollEdge();
        if (mOnScaleChangedListener != null) {
            mOnScaleChangedListener.onScaleChange(scaleFactor, focusX, focusY);
        }
    }
    
    @Override
    public boolean onDrag(float dx, float dy) {
        boolean intercept = mBgAnimStart
            || Math.sqrt((dx * dx) + (dy * dy)) < mViewConfiguration.getScaledTouchSlop()
            || !hasVisibleDrawable();
        
        if (!mDragging && intercept) {
            return false;
        }
        
        if (getScale() > 1) {
            return dragWhenScaleThanOne(dx, dy);
        }
        
        if (!mDragging && Math.abs(dx) > Math.abs(dy)) {
            return false;
        }
        
        if (!mDragging) {
            // 执行拖拽操作，请求父类不要拦截请求
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        
        mDragging = true;
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
        
        mHelper.mRootViewBgMask.getBackground().setAlpha(mIntAlpha);
        mHelper.showThumbnailViewMask(mIntAlpha >= 255);
        
        if (scrollY < 0 && scale >= 0.6) {
            // 更改大小
            setScale(scale);
        }
        return true;
    }
    
    /**
     * 处理图片如果超出控件大小时的滑动
     */
    private boolean dragWhenScaleThanOne(float dx, float dy) {
        boolean dxBigDy = Math.abs(dx) > Math.abs(dy);
        if (mDragging) {
            dx *= 0.2f;
            dy *= 0.2f;
            int scrollX = (int) (getScrollX() - dx);
            int scrollY = (int) (getScrollY() - dy);
            int width = (int) (getWidth() * 0.2);
            int height = (int) (getHeight() * 0.2);
            if (Math.abs(scrollX) > width) {
                dx = 0;
            }
            
            if (Math.abs(scrollY) > height) {
                dy = 0;
            }
            
            if (dxBigDy) {
                dy = 0;
            } else {
                dx = 0;
            }
            
            // 移动图像
            scrollBy(((int) -dx), ((int) -dy));
            return true;
        } else {
            int verticalScrollEdge = attacher.getVerticalScrollEdge();
            int horizontalScrollEdge = attacher.getHorizontalScrollEdge();
            boolean isTop = verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_TOP
                || verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTH;
            boolean isBottom = verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTTOM
                || verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTH;
            boolean isStart = horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_LEFT
                || horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_BOTH;
            boolean isEnd = horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_RIGHT
                || horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_BOTH;
            boolean isVerticalScroll = !dxBigDy && ((isTop && dy > 0) || (isBottom && dy < 0));
            boolean isHorizontalScroll = dxBigDy && ((mStartView && isStart && dx > 0) || (mEndView && isEnd && dx < 0));
            if ((isVerticalScroll && mScaleVerticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE)
                || (isHorizontalScroll && mScaleHorizontalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE)) {
                // 执行拖拽操作，请求父类不要拦截请求
                ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                
                mDragging = true;
                // 移动图像
                scrollBy(((int) -dx), ((int) -dy));
                return true;
            }
        }
        return false;
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
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }
    
    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }
    
    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }
    
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }
    
    void setPhotoPreviewHelper(PhotoPreviewHelper helper) {
        mHelper = helper;
    }
    
    void setImageChangeListener(ImageChangeListener listener) {
        mImageChangeListener = listener;
    }
    
    public void setStartView(boolean isStartView) {
        mStartView = isStartView;
    }
    
    public void setEndView(boolean isEndView) {
        mEndView = isEndView;
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
}
