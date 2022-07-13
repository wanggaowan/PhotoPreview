package com.wgw.photo.preview.interfaces;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 图片被长按监听
 *
 * @author Created by wanggaowan on 2019/3/6 0006 17:19
 */
public interface OnImageLongClickListener {
    /**
     * 长按，可添加自定义处理选项，比如保存图片、分享等
     *
     * @param position  被点击图片位置
     * @param imageView 展示被点击图片的ImageView
     */
    boolean onLongClick(int position, ImageView imageView);
}
