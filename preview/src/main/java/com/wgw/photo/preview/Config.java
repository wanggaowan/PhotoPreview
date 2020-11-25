package com.wgw.photo.preview;

import android.graphics.drawable.Drawable;

import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnDismissListener;
import com.wgw.photo.preview.interfaces.OnLongClickListener;

import java.util.List;

/**
 * 预览配置
 *
 * @author Created by wanggaowan on 11/20/20 10:33 PM
 */
public class Config {
    public ImageLoader imageLoader;
    public int indicatorType = IndicatorType.DOT;
    public int maxIndicatorDot = 9;
    public int selectIndicatorColor = 0xFFFFFFFF/*白色*/;
    public int normalIndicatorColor = 0xFFAAAAAA/*灰色*/;
    public Drawable progressDrawable/*ProgressBar默认样式*/;
    public Integer progressColor;
    public long delayShowProgressTime = 100;
    public OnLongClickListener onLongClickListener;
    public OnDismissListener onDismissListener;
    public Boolean fullScreen/*默认跟随打开预览的界面显示模式*/;
    public List<Object> sources;
    public int defaultShowPosition;
    public Long animDuration/*打开和退出预览时的过度动画时间*/;
}
