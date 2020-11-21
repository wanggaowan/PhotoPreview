package com.wgw.photo.preview.interfaces;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * load image
 *
 * @author Created by wanggaowan on 2019/2/27 0027 10:32
 */
public interface ImageLoader {
    /**
     * 加载图片
     *
     * @param position  图片位置
     * @param source    图片数据
     * @param imageView 展示图片的控件
     */
    void onLoadImage(int position, @Nullable Object source, @NonNull ImageView imageView);
}
