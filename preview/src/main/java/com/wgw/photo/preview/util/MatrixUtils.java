package com.wgw.photo.preview.util;

import android.graphics.Matrix;

/**
 * @author Created by wanggaowan on 11/19/20 10:27 PM
 */
public class MatrixUtils {
    private static final float[] mMatrixValues = new float[9];
    
    public static float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }
    
    public static float getScale(Matrix matrix) {
        return (float) Math.sqrt((float) Math.pow(getValue(matrix, Matrix.MSCALE_X), 2)
            + (float) Math.pow(getValue(matrix, Matrix.MSKEW_Y), 2));
    }
}
