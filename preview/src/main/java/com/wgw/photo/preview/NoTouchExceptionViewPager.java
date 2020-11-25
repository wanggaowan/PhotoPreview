package com.wgw.photo.preview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.chrisbanes.photoview.PhotoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * 捕获触摸异常,主要是{@link PhotoView}与Viewpager结合使用有bug，目前作者未修复，给出捕获异常解决方案
 *
 * @author Created by wanggaowan on 2019/2/28 0028 11:44
 */
class NoTouchExceptionViewPager extends ViewPager {
    
    private boolean mTouchEnable;
    
    public NoTouchExceptionViewPager(@NonNull Context context) {
        super(context);
    }
    
    public NoTouchExceptionViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (!mTouchEnable) {
                return false;
            }
            
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            if (ev.getPointerCount() > 1) {
                return false;
            }
            
            return super.onTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }
    
    public void setTouchEnable(boolean touchEnable) {
        mTouchEnable = touchEnable;
    }
}
