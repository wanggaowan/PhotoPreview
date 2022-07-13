package com.wgw.photo.preview;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.wgw.photo.preview.PhotoPreviewHelper.OnExitListener;
import com.wgw.photo.preview.PhotoPreviewHelper.OnOpenListener;
import com.wgw.photo.preview.PreloadImageView.DrawableLoadListener;
import com.wgw.photo.preview.interfaces.IFindThumbnailView;
import com.wgw.photo.preview.interfaces.OnImageLongClickListener;
import com.wgw.photo.preview.interfaces.OnLongClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 整个预览库都需要共享的数据
 *
 * @author Created by wanggaowan on 11/24/20 8:46 PM
 */
class ShareData {
    
    @NonNull
    final Config config = new Config();
    
    /**
     * 打开预览时的缩略图
     */
    @Nullable
    View thumbnailView;
    
    /**
     * 获取指定位置的缩略图
     */
    @Nullable
    IFindThumbnailView findThumbnailView;
    
    /**
     * 图片长按监听
     */
    @Nullable
    OnImageLongClickListener onLongClickListener;
    
    /**
     * 预览退出监听
     */
    @Nullable
    OnExitListener onExitListener;
    
    /**
     * 预览打开监听
     */
    @Nullable
    OnOpenListener onOpenListener;
    
    /**
     * 是否需要执行进入动画
     */
    boolean showNeedAnim;
    
    /**
     * 预览界面是否第一次创建
     */
    boolean isFirstCreate = true;
    
    /**
     * 预览动画延迟执行时间
     */
    long openAnimDelayTime;
    
    /**
     * 预加载图片，加载内容为默认打开数据
     */
    Drawable preLoadDrawable;
    
    /**
     * 预加载图片监听
     */
    DrawableLoadListener preDrawableLoadListener;
    
    void applyConfig(Config config) {
        this.config.apply(config);
    }
    
    void release() {
        config.release();
        thumbnailView = null;
        findThumbnailView = null;
        onLongClickListener = null;
        onExitListener = null;
        onOpenListener = null;
        showNeedAnim = false;
        isFirstCreate = true;
        openAnimDelayTime = 0;
        preLoadDrawable = null;
        preDrawableLoadListener = null;
    }
}
