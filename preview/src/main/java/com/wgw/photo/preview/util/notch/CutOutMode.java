package com.wgw.photo.preview.util.notch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

/**
 * 异形屏全屏适配方案
 *
 * @author Created by 汪高皖 on 2019/3/12 0012 09:55
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef({CutOutMode.DEFAULT, CutOutMode.SHORT_EDGES, CutOutMode.NEVER, CutOutMode.ALWAYS})
public @interface CutOutMode {
    /**
     * 默认模式，在全屏状态下，效果与{@link #NEVER}一致，在非全屏状态下，竖屏时绘制到耳朵区域，横屏时禁用耳朵区
     */
    int DEFAULT = 0;
    
    /**
     * 耳朵区域绘制模式，在androidP(包含)以上机型，横竖屏都绘制到耳朵区域。
     * 此版本之下，小米手机只竖屏绘制到耳朵区域
     */
    int SHORT_EDGES = 1;
    
    /**
     * 耳朵区域不绘制模式，此时全屏时，状态栏呈现黑条
     */
    int NEVER = 2;
    
    /**
     * 横竖屏都绘制到耳朵区域,如果可以单独设置的情况下
     */
    int ALWAYS = 3;
}
