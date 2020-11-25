package com.github.chrisbanes.photoview.custom;

import android.widget.ImageView;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * Callback when the user tapped outside of the photo
 */
@RestrictTo(Scope.LIBRARY)
public interface OnOutsidePhotoTapListener {

    /**
     * The outside of the photo has been tapped
     */
    void onOutsidePhotoTap(ImageView imageView);
}
