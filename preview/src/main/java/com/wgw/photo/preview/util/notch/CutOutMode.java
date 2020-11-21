package com.wgw.photo.preview.util.notch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

/**
 * 刘海屏全屏适配方案
 *
 * @author Created by 汪高皖 on 2019/3/12 0012 09:55
 */
@Target(ElementType.PARAMETER)
@IntDef({CutOutMode.DEFAULT,CutOutMode.SHORT_EDGES,CutOutMode.NEVER})
public @interface CutOutMode {
    /**
     * 默认模式，在非沉浸式状态下，效果与{@link #NEVER}一致，在沉浸式状态下，效果与{@link #SHORT_EDGES}一致
     */
    int DEFAULT = 0;
    
    /**
     * 刘海区域绘制模式，内容会延伸至刘海区域
     */
    int SHORT_EDGES = 1;
    
    /**
     * 刘海区域不绘制模式，此时全屏时，状态栏呈现黑条
     */
    int NEVER = 2;
}
