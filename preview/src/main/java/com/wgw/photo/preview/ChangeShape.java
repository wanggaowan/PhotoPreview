/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wgw.photo.preview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Outline;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.transition.Transition;
import androidx.transition.TransitionValues;

/**
 * 图形变换，目前只有圆角变换
 */
class ChangeShape extends Transition {
    
    private static final String PROPNAME_RADIUS = "android:ChangeShape:radius";
    private static final String[] sTransitionProperties = {
        PROPNAME_RADIUS,
    };
    
    private static class Property extends android.util.Property<View, Float> {
        
        private ViewOutlineProvider mProvider;
        private final float startValue;
        private final float endValue;
        
        /**
         * A constructor that takes an identifying name and {@link #getType() type} for the property.
         *
         * @param startValue 属性改变的起始值
         * @param endValue   属性改变的结束值
         */
        public Property(float startValue, float endValue) {
            super(Float.class, "radius");
            this.startValue = startValue;
            this.endValue = endValue;
        }
        
        @Override
        public void set(View view, Float value) {
            if (value == null || (startValue <= endValue && value < endValue * 0.01) // 退出预览，此时圆角小于endValue * 0.01不做处理
                || (startValue > endValue && value < startValue * 0.01)) { // 打开预览，此时圆角小于startValue * 0.01不做处理
                // TODO: 12/21/20 wanggaowan 如果不做此判断，那么动画结束时会闪屏,目前不清楚为什么出现该情况
                return;
            }
            
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                if (mProvider == null) {
                    mProvider = new ViewOutlineProvider();
                }
                
                mProvider.setRadius(value);
                view.setOutlineProvider(mProvider);
            } else if (view instanceof CardView) {
                ((CardView) view).setRadius(value);
            }
        }
        
        @Override
        public Float get(View object) {
            return null;
        }
    }
    
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    static class ViewOutlineProvider extends android.view.ViewOutlineProvider {
        
        private float radius = 0f;
        
        public void setRadius(float radius) {
            this.radius = radius;
        }
        
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(view.getLeft(), view.getTop(),
                view.getLeft() + view.getWidth(),
                view.getTop() + view.getHeight(),
                radius);
        }
    }
    
    private final float startRadius;
    private final float endRadius;
    
    public ChangeShape(float startRadius, float endRadius) {
        this.startRadius = startRadius;
        this.endRadius = endRadius;
    }
    
    @Nullable
    @Override
    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }
    
    @Override
    public void captureStartValues(@NonNull TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_RADIUS, startRadius);
    }
    
    @Override
    public void captureEndValues(@NonNull TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_RADIUS, endRadius);
    }
    
    @Override
    @Nullable
    public Animator createAnimator(@NonNull final ViewGroup sceneRoot,
                                   @Nullable TransitionValues startValues, @Nullable TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        
        Float startRadius = (Float) startValues.values.get(PROPNAME_RADIUS);
        Float endRadius = (Float) endValues.values.get(PROPNAME_RADIUS);
        if (startRadius == null || endRadius == null || startRadius.equals(endRadius)) {
            return null;
        }
        
        return ofFloat(endValues.view, startRadius, endRadius);
    }
    
    private ObjectAnimator ofFloat(View target, float startValue, float endValue) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            target.setClipToOutline(true);
            return ObjectAnimator.ofFloat(target, new Property(startValue, endValue), startValue, endValue);
        }
        
        if (target instanceof CardView) {
            return ObjectAnimator.ofFloat((CardView) target, "radius", startValue, endValue);
        }
        
        return null;
    }
}
