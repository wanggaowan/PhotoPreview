package com.wgw.photo.preview.interfaces;

import android.view.View;

/**
 * 查找预览图指定下标对应的缩略图控件
 *
 * @author Created by wanggaowan on 11/20/20 9:16 PM
 */
public interface IFindThumbnailView {
    /**
     * 返回对象用于打开和关闭过度动画
     *
     * @param position 预览位置
     * @return 预览图对应的缩略图控件
     */
    View findView(int position);
}
