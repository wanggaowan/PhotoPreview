package com.github.chrisbanes.photoview;


import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * Interface definition for callback to be invoked when attached ImageView scale changes
 */
@RestrictTo(Scope.LIBRARY)
public interface OnScaleChangedListener {

    /**
     * Callback for when the scale changes
     *
     * @param scaleFactor the scale factor (less than 1 for zoom out, greater than 1 for zoom in)
     * @param focusX      focal point X position
     * @param focusY      focal point Y position
     */
    void onScaleChange(float scaleFactor, float focusX, float focusY);
}
