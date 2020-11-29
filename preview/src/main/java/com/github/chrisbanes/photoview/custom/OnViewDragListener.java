package com.github.chrisbanes.photoview.custom;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * Interface definition for a callback to be invoked when the photo is experiencing a drag event
 */
@RestrictTo(Scope.LIBRARY)
public interface OnViewDragListener {
    
    /**
     * Callback for when the photo is experiencing a drag event. This cannot be invoked when the
     * user is scaling.
     *
     * @param dx The change of the coordinates in the x-direction
     * @param dy The change of the coordinates in the y-direction
     * @return 返回值表示是否消费此次事件
     */
    boolean onDrag(float dx, float dy);
}
