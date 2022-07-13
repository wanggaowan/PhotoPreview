package com.wgw.photo.preview.interfaces;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * call when preview view long press
 *
 * @author Created by wanggaowan on 2019/3/6 0006 17:19
 */
public interface OnLongClickListener {
    /**
     * 长按，可添加自定义处理选项，比如保存图片、分享等
     *
     * @param position       被点击图片位置
     * @param customViewRoot 自定义View根布局,全局唯一,可将自定义View加入到customViewRoot中。默认显示状态为{@link View#GONE}
     * @param imageView      展示被点击图片的ImageView
     */
    boolean onLongClick(int position, FrameLayout customViewRoot, ImageView imageView);
}
