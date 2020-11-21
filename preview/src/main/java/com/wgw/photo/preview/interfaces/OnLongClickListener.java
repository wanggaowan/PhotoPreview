package com.wgw.photo.preview.interfaces;

import android.widget.FrameLayout;

/**
 * call when preview view long press
 *
 * @author Created by wanggaowan on 2019/3/6 0006 17:19
 */
public interface OnLongClickListener {
    /**
     * @param rootView 当前预览图片根布局
     */
    void onLongClick(FrameLayout rootView);
}
