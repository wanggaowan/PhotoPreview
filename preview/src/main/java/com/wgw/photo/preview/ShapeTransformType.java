package com.wgw.photo.preview;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

/**
 * 图形变换类型
 *
 * @author Created by wanggaowan on 12/21/20 6:50 PM
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef({ShapeTransformType.CIRCLE, ShapeTransformType.ROUND_RECT})
public @interface ShapeTransformType {
    /**
     * 切圆形，预览动画从圆形变换为矩形
     */
    int CIRCLE = 0;
    
    /**
     * 切圆角矩形，预览动画从圆角矩形变换为矩形
     */
    int ROUND_RECT = 1;
}
