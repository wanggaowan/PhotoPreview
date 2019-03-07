package com.wgw.photo.preview.interfaces;

import android.widget.ImageView;

/**
 * 图片加载接口
 *
 * @author Created by 汪高皖 on 2019/2/27 0027 10:32
 */
public interface ImageLoader {
    /**
     * 加载图片
     *
     * @param position  图片位置
     * @param object    图片数据
     * @param imageView 展示图片的控件
     */
    void onLoadImage(int position, Object object, ImageView imageView);
}
