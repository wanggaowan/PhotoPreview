package com.wgw.photo.preview.interfaces;

import android.widget.FrameLayout;

/**
 * 图片长按点击监听
 *
 * @author Created by 汪高皖 on 2019/3/6 0006 17:19
 */
public interface OnLongClickListener {
    /**
     * @param rootView 当前预览图片根布局
     */
    void onLongClick(FrameLayout rootView);
}
