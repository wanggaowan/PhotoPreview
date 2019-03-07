package com.wgw.photo.preview;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Created by 汪高皖 on 2019/3/6 0006 17:15
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef({IndicatorType.DOT,IndicatorType.TEXT})
public @interface IndicatorType {
    /**
     * 圆点
     */
    int DOT = 0;
    
    /**
     * 文本
     */
    int TEXT = 1;
}
