package com.wgw.photo.preview;

import android.view.View;
import android.view.View.OnLongClickListener;

import com.wgw.photo.preview.PhotoPreviewFragment.OnExitListener;
import com.wgw.photo.preview.PhotoPreviewFragment.OnOpenListener;
import com.wgw.photo.preview.interfaces.IFindThumbnailView;

import androidx.annotation.NonNull;

/**
 * 整个预览库都需要共享的数据
 *
 * @author Created by wanggaowan on 11/24/20 8:46 PM
 */
class ShareData {
    
    @NonNull
    final Config config;
    
    /**
     * 打开预览时的缩略图
     */
    View thumbnailView;
    
    /**
     * 获取指定位置的缩略图
     */
    IFindThumbnailView findThumbnailView;
    
    /**
     * 图片长按监听
     */
    OnLongClickListener onLongClickListener;
    
    /**
     * 预览退出监听
     */
    OnExitListener onExitListener;
    
    /**
     * 预览打开监听
     */
    OnOpenListener onOpenListener;
    
    /**
     * 是否需要执行进入动画
     */
    boolean showNeedAnim;
    
    ShareData(@NonNull Config config) {this.config = config;}
}
