package com.wgw.photo.preview;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

/**
 * 图片指示器类型
 *
 * @author Created by wanggaowan on 2019/3/6 0006 17:15
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef({IndicatorType.DOT, IndicatorType.TEXT})
public @interface IndicatorType {
    /**
     * 圆点,如果图片多于九张，则设置该类型也采用TEXT类型
     */
    int DOT = 0;
    
    /**
     * 文本
     */
    int TEXT = 1;
}
