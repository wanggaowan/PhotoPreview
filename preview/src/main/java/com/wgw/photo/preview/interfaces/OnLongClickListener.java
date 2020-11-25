package com.wgw.photo.preview.interfaces;

import android.view.View;
import android.widget.FrameLayout;

/**
 * call when preview view long press
 *
 * @author Created by wanggaowan on 2019/3/6 0006 17:19
 */
public interface OnLongClickListener {
    /**
     * 长按，可添加自定义处理选项，比如保存图片、分享等
     *
     * @param rootView 当前预览根布局,默认显示状态为{@link View#GONE}
     */
    boolean onLongClick(FrameLayout rootView);
}
