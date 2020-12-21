package com.wgw.photo.preview;

import android.graphics.drawable.Drawable;

import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnDismissListener;
import com.wgw.photo.preview.interfaces.OnLongClickListener;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * 预览配置
 *
 * @author Created by wanggaowan on 11/20/20 10:33 PM
 */
public class Config {
    @Nullable
    public ImageLoader imageLoader;
    public int indicatorType = IndicatorType.DOT;
    public int maxIndicatorDot = 9;
    public int selectIndicatorColor = 0xFFFFFFFF/*白色*/;
    public int normalIndicatorColor = 0xFFAAAAAA/*灰色*/;
    @Nullable
    public Drawable progressDrawable/*ProgressBar默认样式*/;
    @Nullable
    public Integer progressColor;
    public long delayShowProgressTime = 100;
    @Nullable
    public OnLongClickListener onLongClickListener;
    @Nullable
    public OnDismissListener onDismissListener;
    @Nullable
    public Boolean fullScreen/*默认跟随打开预览的界面显示模式*/;
    @Nullable
    public List<?> sources;
    public int defaultShowPosition = 0;
    @Nullable
    public Long animDuration/*打开和退出预览时的过度动画时间*/;
    /**
     * 图形变换类型，可选值参考{@link ShapeTransformType}
     */
    @Nullable
    public Integer shapeTransformType;
    /**
     * 图形变换设置为{@link ShapeTransformType#ROUND_RECT}时圆角半径
     */
    public int shapeCornerRadius = 0;
    
    public void apply(Config config) {
        if (config == null) {
            return;
        }
        
        this.imageLoader = config.imageLoader;
        this.indicatorType = config.indicatorType;
        this.maxIndicatorDot = config.maxIndicatorDot;
        this.selectIndicatorColor = config.selectIndicatorColor;
        this.normalIndicatorColor = config.normalIndicatorColor;
        this.progressDrawable = config.progressDrawable;
        this.progressColor = config.progressColor;
        this.delayShowProgressTime = config.delayShowProgressTime;
        this.onLongClickListener = config.onLongClickListener;
        this.onDismissListener = config.onDismissListener;
        this.fullScreen = config.fullScreen;
        this.sources = config.sources;
        this.defaultShowPosition = config.defaultShowPosition;
        this.animDuration = config.animDuration;
        this.shapeTransformType = config.shapeTransformType;
        this.shapeCornerRadius = config.shapeCornerRadius;
    }
    
    void release() {
        this.imageLoader = null;
        this.indicatorType = IndicatorType.DOT;
        this.maxIndicatorDot = 9;
        this.selectIndicatorColor = 0xFFFFFFFF;
        this.normalIndicatorColor = 0xFFAAAAAA;
        this.progressDrawable = null;
        this.progressColor = null;
        this.delayShowProgressTime = 100;
        this.onLongClickListener = null;
        this.onDismissListener = null;
        this.fullScreen = null;
        this.sources = null;
        this.defaultShowPosition = 0;
        this.animDuration = null;
        this.shapeTransformType = null;
        this.shapeCornerRadius = 0;
    }
}
