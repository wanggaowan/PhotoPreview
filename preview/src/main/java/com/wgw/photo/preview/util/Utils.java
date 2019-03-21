package com.wgw.photo.preview.util;

import android.content.Context;
import android.graphics.Matrix;
import android.util.TypedValue;

/**
 * @author Created by 汪高皖 on 2019/2/28 0028 13:59
 */
public class Utils {
    public static int dp2px(Context context, int dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dipValue, context.getResources().getDisplayMetrics());
    }
    
    public static float getScale(Matrix matrix) {
        return (float) Math.sqrt((float) Math.pow(getValue(matrix, Matrix.MSCALE_X), 2) + (float) Math.pow
            (getValue(matrix, Matrix.MSKEW_Y), 2));
    }
    
    private static float getValue(Matrix matrix, int whichValue) {
        float[] mMatrixValues = new float[9];
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }
}
