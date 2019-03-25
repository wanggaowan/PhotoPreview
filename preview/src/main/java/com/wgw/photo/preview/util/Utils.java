package com.wgw.photo.preview.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author Created by 汪高皖 on 2019/2/28 0028 13:59
 */
public class Utils {
    public static int dp2px(Context context, int dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dipValue, context.getResources().getDisplayMetrics());
    }
    
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
            .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
