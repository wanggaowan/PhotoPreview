package com.wgw.photo.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;

import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.wgw.photo.preview.util.MatrixUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.Lifecycle.State;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

/**
 * 图片预览Fragment
 *
 * @author Created by wanggaowan on 2019/2/27 0027 11:24
 */
@RestrictTo(Scope.LIBRARY)
public class PhotoPreviewFragment extends Fragment {
    private static final long OPEN_AND_EXIT_ANIM_DURATION = 200;
    private static final long OPEN_AND_EXIT_ANIM_DURATION_FOR_IMAGE = 300;
    
    FrameLayout mRoot;
    private PhotoView mPhotoView;
    // 占位图，辅助执行过度动画
    // 为什么不直接使用mPhotoView进行过度动画，主要有一下几个问题
    // 1. 使用mPhotoView，某些情况下预览打开后图片某一部分自动被放大。导致这个的原因可能是使用Glide加载库，设置了placeholder导致
    // 2. 缩略图缩放类型非CENTER_CROP时，不能使用预览界面的图片进行过度，此时预览图需要设置为缩略图的drawable
    // 3. 根据缩略图设置的不同ScaleType,预览图的加载时间也不同，不清楚是Transition库的问题还是我使用不当(估计使用不当吧)
    private ImageView mHelperView;
    private ProgressBar mLoading;
    
    private ShareData mShareData;
    private View mThumbnailView;
    private ScaleType mThumbnailViewScaleType;
    private int mPosition;
    // 当前界面是否需要执行动画
    private boolean mNeedInAnim;
    // 根据动画时间可决定整个预览是否需要执行动画
    private long mAnimDuration;
    
    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();
    private final int[] mIntTemp = new int[2];
    private final int[] mSrcImageSize = new int[2];
    private final int[] mSrcImageLocation = new int[2];
    private final float[] mFloatTemp = new float[2];
    // 记录预览界面图片缩放倍率为1时图片真实绘制大小
    private final float[] mNoScaleImageActualSize = new float[2];
    
    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRoot == null) {
            mRoot = (FrameLayout) inflater.inflate(R.layout.fragment_preview, null);
            mHelperView = mRoot.findViewById(R.id.iv_anim);
            mPhotoView = mRoot.findViewById(R.id.photoView);
            mPhotoView.setPhotoPreviewFragment(this);
            mLoading = mRoot.findViewById(R.id.loading);
            initEvent();
        }
        
        if (savedInstanceState == null) {
            // 防止预览时有些界面打开输入法，弹窗弹出时，输入法未及时关闭
            mRoot.setFocusableInTouchMode(true);
            mRoot.requestFocus();
            onLoadData();
        }  // savedInstanceState != null的时候，预览窗口（父对象PreviewDialogFragment）将关闭，因此不做任何其它处理
        
        return mRoot;
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        ViewParent parent = mRoot.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mRoot);
        }
    }
    
    private void initEvent() {
        mRoot.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && mPhotoView.getVisibility() == View.VISIBLE) {
                exit();
                return true;
            }
            return false;
        });
        
        mRoot.setOnClickListener(v -> {
            if (mPhotoView.getVisibility() != View.VISIBLE) {
                return;
            }
            exit();
        });
        
        mPhotoView.setOnLongClickListener(v -> {
            if (mShareData != null && mShareData.config != null && mShareData.config.onLongClickListener != null) {
                mShareData.config.onLongClickListener.onLongClick(mRoot);
            }
            return true;
        });
        
        mPhotoView.setOnClickListener(v -> exit());
    }
    
    private void onLoadData() {
        mPhotoView.setImageDrawable(null);
        Fragment parentFragment = getParentFragment();
        if (!(parentFragment instanceof PreviewDialogFragment)) {
            initNoAnim();
            return;
        }
        
        mShareData = ((PreviewDialogFragment) parentFragment).mShareData;
        if (mShareData == null) {
            initNoAnim();
            return;
        }
        
        mPhotoView.setScaleLevels(
            PhotoViewAttacher.DEFAULT_MIN_SCALE,
            PhotoViewAttacher.DEFAULT_MID_SCALE,
            PhotoViewAttacher.DEFAULT_MAX_SCALE);
        
        mThumbnailView = getThumbnailView();
        if (mThumbnailView instanceof ImageView) {
            mThumbnailViewScaleType = ((ImageView) mThumbnailView).getScaleType();
        } else {
            mThumbnailViewScaleType = null;
        }
        
        mAnimDuration = getOpenAndExitAnimDuration(mThumbnailView);
        mNeedInAnim = mAnimDuration > 0 && mShareData.showNeedAnim && mPosition == mShareData.config.defaultShowPosition;
        
        initLoading();
        loadImage(mPhotoView);
        initHelperView();
        
        if (!mNeedInAnim) {
            initNoAnim();
            if (mAnimDuration <= 0 && mShareData.onOpenListener != null) {
                mShareData.onOpenListener.onStart();
                mShareData.onOpenListener.onEnd();
            }
            return;
        }
        
        // 处理进入时的动画
        mNeedInAnim = false;
        mShareData.showNeedAnim = false;
        mRoot.setBackgroundColor(Color.TRANSPARENT);
        if (mShareData.onOpenListener != null) {
            mShareData.onOpenListener.onStart();
        }
        
        doViewBgAnim(Color.BLACK, mAnimDuration, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mShareData.onOpenListener != null) {
                    mShareData.onOpenListener.onEnd();
                }
            }
        });
        
        if (mThumbnailView == null) {
            mPhotoView.setVisibility(View.VISIBLE);
            mHelperView.setVisibility(View.GONE);
            mPhotoView.setMinimumScale(0);
            ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", 0, 1f);
            scaleOa.setDuration(mAnimDuration);
            scaleOa.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPhotoView.setMinimumScale(PhotoViewAttacher.DEFAULT_MIN_SCALE);
                }
            });
            scaleOa.start();
            return;
        }
        
        enterAnimByTransition(mThumbnailView);
    }
    
    private void initNoAnim() {
        mRoot.setBackgroundColor(Color.BLACK);
        mHelperView.setVisibility(View.GONE);
        mPhotoView.setVisibility(View.VISIBLE);
    }
    
    // 初始化辅助类
    private void initHelperView() {
        if (mThumbnailViewScaleType != null) {
            // 此时提供的是ImageView缩略图，不再此处初始化，需要根据不同ScaleType，在不同时间点执行初始化
            return;
        }
        
        loadImage(mHelperView);
    }
    
    /**
     * 使用Transition库实现过度动画
     */
    private void enterAnimByTransition(final View thumbnailView) {
        mPhotoView.setVisibility(View.INVISIBLE);
        mHelperView.setVisibility(View.VISIBLE);
        mPhotoView.post(() -> {
            if (mThumbnailViewScaleType == ScaleType.CENTER_CROP && mPhotoView.getDrawable() == null) {
                // 如果是图片空间 Transition很依赖drawable大小和ScaleType，因此在获取不到图片的情况下，采用传统过度模式
                enterAnimByNormal(thumbnailView);
                return;
            }
            
            getSrcViewSize(thumbnailView);
            getSrcViewLocation(thumbnailView);
            float fromX = mSrcImageLocation[0];
            float fromY = mSrcImageLocation[1];
            mHelperView.setTranslationX(fromX);
            mHelperView.setTranslationY(fromY);
            setViewSize(mHelperView, mSrcImageSize[0], mSrcImageSize[1]);
            if (mThumbnailViewScaleType != null) {
                // 说明是ImageView
                mHelperView.setScaleType(mThumbnailViewScaleType);
                if (mThumbnailViewScaleType != ScaleType.CENTER_CROP) {
                    // 非CENTER_CROP类型，一定要在此处初始化 ImageDrawable，mIvAnim设置大小和ScaleType之后
                    mHelperView.setImageDrawable(((ImageView) thumbnailView).getDrawable());
                }
            }
            
            mHelperView.post(() -> {
                TransitionSet transitionSet = new TransitionSet()
                    .setDuration(mAnimDuration)
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform())
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .addListener(new TransitionListenerAdapter() {
                        @Override
                        public void onTransitionEnd(@NonNull Transition transition) {
                            mPhotoView.setVisibility(View.VISIBLE);
                            mHelperView.setVisibility(View.GONE);
                        }
                    });
                TransitionManager.beginDelayedTransition((ViewGroup) mHelperView.getParent(), transitionSet);
                
                mHelperView.setTranslationY(0);
                mHelperView.setTranslationX(0);
                mHelperView.setScaleType(ScaleType.FIT_CENTER);
                setViewSize(mHelperView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            });
        });
    }
    
    /**
     * 使用常规方式实现过度动画
     */
    private void enterAnimByNormal(final View thumbnailView) {
        mPhotoView.setVisibility(View.VISIBLE);
        mHelperView.setVisibility(View.GONE);
        mPhotoView.post(() -> {
            getSrcViewSize(thumbnailView);
            float scaleX = mPhotoView.getWidth() <= 0 ? 0 : mSrcImageSize[0] * 1f / mPhotoView.getWidth();
            float scaleY = mPhotoView.getHeight() <= 0 ? 0 : mSrcImageSize[1] * 1f / mPhotoView.getHeight();
            float scale = 0;
            if (scaleX > 0 && scaleY > 0) {
                Matrix m = new Matrix();
                m.postScale(scaleX, scaleY);
                scale = MatrixUtils.getScale(m);
            }
            
            getSrcViewLocation(thumbnailView);
            mSrcImageLocation[0] += thumbnailView.getMeasuredWidth() / 2;
            mSrcImageLocation[1] += thumbnailView.getMeasuredHeight() / 2;
            float fromX = mSrcImageLocation[0] - mPhotoView.getWidth() / 2f;
            float toX = 0;
            float fromY = mSrcImageLocation[1] - mPhotoView.getHeight() / 2f + mPhotoView.getScrollY();
            float toY = 0;
            
            ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", scale, 1f);
            ObjectAnimator xOa = ObjectAnimator.ofFloat(mPhotoView, "translationX", fromX, toX);
            ObjectAnimator yOa = ObjectAnimator.ofFloat(mPhotoView, "translationY", fromY, toY);
            
            AnimatorSet set = new AnimatorSet();
            set.setDuration(mAnimDuration);
            set.playTogether(scaleOa, xOa, yOa);
            set.start();
        });
    }
    
    /**
     * 加载图片
     */
    private void loadImage(ImageView imageView) {
        if (mShareData.config.imageLoader != null) {
            if (mShareData.config.sources != null && mPosition < mShareData.config.sources.size() && mPosition >= 0) {
                mShareData.config.imageLoader.onLoadImage(mPosition, mShareData.config.sources.get(mPosition), imageView);
            } else {
                mShareData.config.imageLoader.onLoadImage(mPosition, null, imageView);
            }
        }
    }
    
    /**
     * 设置View的大小
     */
    private void setViewSize(View target, int width, int height) {
        ViewGroup.LayoutParams params = target.getLayoutParams();
        params.width = width;
        params.height = height;
        target.setLayoutParams(params);
    }
    
    /**
     * 初始化loading
     */
    private void initLoading() {
        if (mAnimDuration > 0) {
            mPhotoView.setDrawEndListener(this :: getOpenDrawableSize);
        }
        
        mPhotoView.setImageChangeListener(drawable -> {
            if (drawable != null) {
                mLoading.setVisibility(View.GONE);
                if (mAnimDuration <= 0) {
                    return;
                }
                
                if (!mNeedInAnim) {
                    // 当前界面未执行进入时动画，因此未初始化mIvAnim状态，此处初始化，用于退出时使用
                    if (mThumbnailViewScaleType != null) {
                        // 只有ThumbnailView 为imageView才需要初始化
                        if (mThumbnailViewScaleType == ScaleType.CENTER_CROP) {
                            mHelperView.setImageDrawable(mPhotoView.getDrawable());
                        } else {
                            mHelperView.setImageDrawable(((ImageView) mThumbnailView).getDrawable());
                        }
                    }
                }
                
                // 进入时需要执行动画，以下设置mIvAnim ImageDrawable必须放在此处调用，如果放在enterAnimByTransition调用，则过渡动画将出现问题
                if (mNeedInAnim && mHelperView.getDrawable() == null) {
                    if (mThumbnailViewScaleType == ScaleType.CENTER_CROP) {
                        // 缩略图缩放类型为CENTER_CROP时，用于过度的控件mIvAnim显示的drawable需要与最终界面显示的一致
                        // 否则采用Transition实现过度效果会有问题。出现问题的原因就是不同缩放模式，如果图片大小不一致，最终缩放
                        // 出来的部位是不一致的
                        mHelperView.setImageDrawable(mPhotoView.getDrawable());
                    }
                }
                
                getOpenDrawableSize();
            }
        });
        
        
        if (mShareData.config.delayShowProgressTime < 0) {
            mLoading.setVisibility(View.GONE);
            return;
        }
        
        if (mShareData.config.progressDrawable != null) {
            mLoading.setIndeterminateDrawable(mShareData.config.progressDrawable);
        }
        
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && mShareData.config.progressColor != null) {
            mLoading.setIndeterminateTintList(ColorStateList.valueOf(mShareData.config.progressColor));
        }
        
        mLoading.setVisibility(mShareData.config.delayShowProgressTime == 0 ? View.VISIBLE : View.GONE);
        if (mShareData.config.defaultShowPosition > 0) {
            // 监听指定延迟后图片是否加载成功
            mPhotoView.postDelayed(() -> {
                if (mPhotoView.getDrawable() == null) {
                    mLoading.setVisibility(View.VISIBLE);
                }
            }, mShareData.config.delayShowProgressTime);
        }
    }
    
    /**
     * 获取进入时图片真实大小，由于图片刚进入时，需要等待绘制，所以可能不能及时获取到准确的大小
     */
    private void getOpenDrawableSize() {
        if (mPhotoView.getScale() != 1) {
            return;
        }
        
        // 用于退出时计算移动后最终图像坐标使用
        // 刚设置图片就获取，此时可能获取不成功
        getImageActualSize(mPhotoView, mNoScaleImageActualSize);
        if (mNoScaleImageActualSize[0] > 0) {
            // 计算最大缩放倍率，屏幕大小的三倍
            double ceil = Math.ceil(mRoot.getWidth() / mNoScaleImageActualSize[0]);
            float maxScale = (float) (ceil * 3f);
            if (maxScale < mPhotoView.getMaximumScale()) {
                return;
            }
            
            float midScale = (maxScale + mPhotoView.getMinimumScale()) / 2;
            mPhotoView.setScaleLevels(mPhotoView.getMinimumScale(), midScale, maxScale);
        }
    }
    
    /**
     * 退出预览
     *
     * @return {@code false}:未执行退出逻辑，可能当前界面已关闭或还未创建完成
     */
    public boolean exit() {
        if (!getLifecycle().getCurrentState().isAtLeast(State.STARTED)) {
            return false;
        }
        
        if (mAnimDuration <= 0) {
            if (mShareData.onExitListener != null) {
                mShareData.onExitListener.onStart();
                mShareData.onExitListener.onExit();
            }
            return true;
        }
        
        if (mPhotoView.getDrawable() == null) {
            if (mShareData.onExitListener != null) {
                mShareData.onExitListener.onStart();
            }
            
            doViewBgAnim(Color.TRANSPARENT, mAnimDuration, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mShareData.onExitListener != null) {
                        mShareData.onExitListener.onExit();
                    }
                }
            });
            return true;
        }
        
        doViewBgAnim(Color.TRANSPARENT, mAnimDuration, null);
        if (mThumbnailView == null) {
            mHelperView.setVisibility(View.GONE);
            mPhotoView.setVisibility(View.VISIBLE);
            
            mPhotoView.setMinimumScale(0);
            ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", mPhotoView.getScale(), 0f);
            scaleOa.setDuration(mAnimDuration);
            scaleOa.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mShareData.onExitListener != null) {
                        mShareData.onExitListener.onExit();
                    }
                }
            });
            
            if (mShareData.onExitListener != null) {
                mShareData.onExitListener.onStart();
            }
            
            scaleOa.start();
            return true;
        }
        
        exitAnimByTransition(mThumbnailView);
        return true;
    }
    
    /**
     * 使用Transition库实现过度动画
     */
    private void exitAnimByTransition(final View thumbnailView) {
        mHelperView.setVisibility(View.VISIBLE);
        mPhotoView.setVisibility(View.INVISIBLE);
        if (mThumbnailViewScaleType == ScaleType.MATRIX || mPhotoView.getScale() < 1) {
            // thumbnailView是ScaleType.MATRIX需要执行以下逻辑，不清楚MATRIX规则
            // 目前发现在关闭时，只要mIvAnim宽高设计成实际绘制Drawable相等宽高则过度动画没问题
            
            // 得到关闭时预览图片真实绘制大小
            float[] imageActualSize = mFloatTemp;
            getImageActualSize(mPhotoView, imageActualSize);
            if (mNoScaleImageActualSize[0] == 0 && mNoScaleImageActualSize[1] == 0) {
                // 此时只是降低偏移值，但是这是不准确的
                getImageActualSize(mHelperView, mNoScaleImageActualSize);
            }
            
            // 计算关闭时预览图片真实X,Y坐标
            // mPhotoView向下移动后缩放比例误差值，该值是手动调出来的，不清楚为什么超出了这么多
            // 只有mPhotoView.getScale() < 1时才会出现此误差
            float errorRatio = mPhotoView.getScale() < 1 ? 0.066f : 0;
            // 此处取mRoot.getHeight()而不是mIvAnim.getHeight()是因为如果当前界面非默认预览的界面,
            // 那么mIvAnim.getHeight()获取的并不是可显示区域高度，只是图片实际绘制的高度，因此使用mRoot.getHeight()
            float y = mRoot.getHeight() / 2f - mNoScaleImageActualSize[1] / 2 // 预览图片未移动未缩放时实际绘制drawable左上角Y轴值
                - mPhotoView.getScrollY() // 向下移动的距离，向上移动不会触发关闭
                + imageActualSize[1] * (1 - mPhotoView.getScale() - errorRatio); // 由于在向下移动时，伴随图片缩小，因此需要加上缩小高度
            float x = -mPhotoView.getScrollX() // 向左或向右移动的距离
                + imageActualSize[0] * (1 - mPhotoView.getScale() - errorRatio); // 由于在向下移动时，伴随图片缩小，因此需要加上缩小宽度
            mHelperView.setTranslationX(x);
            mHelperView.setTranslationY(y);
            setViewSize(mHelperView, ((int) imageActualSize[0]), ((int) imageActualSize[1]));
        }
        
        getSrcViewSize(thumbnailView);
        getSrcViewLocation(thumbnailView);
        if (mShareData.onExitListener != null) {
            mShareData.onExitListener.onStart();
        }
        
        mHelperView.post(() -> {
            TransitionSet transitionSet = new TransitionSet()
                .setDuration(mAnimDuration)
                .addTransition(new ChangeBounds())
                .addTransition(new ChangeTransform())
                .addTransition(new ChangeImageTransform())
                .setInterpolator(new FastOutSlowInInterpolator())
                .addListener(new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(@NonNull Transition transition) {
                        if (mShareData.onExitListener != null) {
                            mShareData.onExitListener.onExit();
                        }
                    }
                });
            
            TransitionManager.beginDelayedTransition((ViewGroup) mHelperView.getParent(), transitionSet);
            
            mHelperView.setTranslationX(mSrcImageLocation[0]);
            mHelperView.setTranslationY(mSrcImageLocation[1]);
            if (mThumbnailViewScaleType != null) {
                mHelperView.setScaleType(mThumbnailViewScaleType);
            }
            setViewSize(mHelperView, mSrcImageSize[0], mSrcImageSize[1]);
        });
    }
    
    /**
     * 使用常规方式实现过度动画
     */
    private void exitAnimByNormal(View thumbnailView) {
        mHelperView.setVisibility(View.GONE);
        mPhotoView.setVisibility(View.VISIBLE);
        float[] imageAcSize = mFloatTemp;
        getImageActualSize(mPhotoView, imageAcSize);
        float scale = 0;
        if (imageAcSize[0] > 0 && imageAcSize[1] > 0) {
            Matrix m = new Matrix();
            getSrcViewSize(thumbnailView);
            m.postScale((mSrcImageSize[0] * 1f / imageAcSize[0]), (mSrcImageSize[1] * 1f / imageAcSize[1]));
            scale = MatrixUtils.getScale(m);
        }
        
        getSrcViewLocation(thumbnailView);
        mSrcImageLocation[0] += thumbnailView.getMeasuredWidth() / 2;
        mSrcImageLocation[1] += thumbnailView.getMeasuredHeight() / 2;
        float toX = mSrcImageLocation[0] - mPhotoView.getWidth() / 2f + mPhotoView.getScrollX();
        float toY = mSrcImageLocation[1] - mPhotoView.getHeight() / 2f + mPhotoView.getScrollY();
        
        mPhotoView.setMinimumScale(0);
        ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", scale * mPhotoView.getScale());
        ObjectAnimator xOa = ObjectAnimator.ofFloat(mPhotoView, "translationX", toX);
        ObjectAnimator yOa = ObjectAnimator.ofFloat(mPhotoView, "translationY", toY);
        
        AnimatorSet set = new AnimatorSet();
        set.setDuration(mAnimDuration);
        set.playTogether(scaleOa, xOa, yOa);
        scaleOa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mShareData.onExitListener != null) {
                    mShareData.onExitListener.onExit();
                }
            }
        });
        
        if (mShareData.onExitListener != null) {
            mShareData.onExitListener.onStart();
        }
        
        set.start();
    }
    
    void doViewBgAnim(final int endColor, long duration, AnimatorListenerAdapter listenerAdapter) {
        final int start = ((ColorDrawable) mRoot.getBackground()).getColor();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.addUpdateListener(animation ->
            mRoot.setBackgroundColor((int) mArgbEvaluator.evaluate(animation.getAnimatedFraction(), start, endColor)));
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        if (listenerAdapter != null) {
            animator.addListener(listenerAdapter);
        }
        animator.start();
    }
    
    /**
     * 获取动画时间
     */
    private long getOpenAndExitAnimDuration(View thumbnailView) {
        if (mShareData.config.animDuration != null) {
            return mShareData.config.animDuration;
        }
        
        if (thumbnailView instanceof ImageView) {
            return OPEN_AND_EXIT_ANIM_DURATION_FOR_IMAGE;
        }
        return OPEN_AND_EXIT_ANIM_DURATION;
    }
    
    /**
     * 获取ImageView实际绘制的图片大小,如果没有设置图片，则返回数据为0。
     * 该方法调用时机不同，返回值有很大差别，如果刚设置imageView drawable，
     * 则可能返回的是drawable原图大小，而不是在imageView中实际绘制出来的大小
     *
     * @return int[2], 下标0：实际绘制图片的宽度，下标1：实际绘制图片高度
     */
    private void getImageActualSize(ImageView imageView, float[] size) {
        size[0] = 0;
        size[1] = 0;
        if (imageView == null || imageView.getDrawable() == null) {
            return;
        }
        
        Drawable drawable = imageView.getDrawable();
        // 获得ImageView中Image的真实宽高，
        int dw = drawable.getBounds().width();
        int dh = drawable.getBounds().height();
        
        // 获得ImageView中Image的变换矩阵
        Matrix m = imageView.getImageMatrix();
        // Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
        float sx = MatrixUtils.getValue(m, Matrix.MSCALE_X);
        float sy = MatrixUtils.getValue(m, Matrix.MSCALE_Y);
        
        // 计算Image在屏幕上实际绘制的宽高
        size[0] = dw * sx;
        size[1] = dh * sy;
    }
    
    /**
     * 获取当前预览图对应的缩略图View,如果未找到则查找默认位置View
     */
    @Nullable
    private View getThumbnailView() {
        View view = getThumbnailViewNotDefault(mPosition);
        if (view == null) {
            if (mPosition != mShareData.config.defaultShowPosition) {
                return getThumbnailViewNotDefault(mShareData.config.defaultShowPosition);
            }
        }
        return view;
    }
    
    /**
     * 获取指定位置的缩略图
     */
    @Nullable
    private View getThumbnailViewNotDefault(int position) {
        if (mThumbnailView != null) {
            return mThumbnailView;
        }
        
        if (mShareData.thumbnailView != null) {
            return mShareData.thumbnailView;
        } else if (mShareData.findThumbnailView != null) {
            return mShareData.findThumbnailView.findView(position);
        }
        
        return null;
    }
    
    /**
     * 获取指定位置的略图的大小.
     * 结果存储在{@link #mSrcImageSize}
     */
    private void getSrcViewSize(View view) {
        mSrcImageSize[0] = 0;
        mSrcImageSize[1] = 0;
        
        if (view == null) {
            return;
        }
        
        mSrcImageSize[0] = view.getMeasuredWidth();
        mSrcImageSize[1] = view.getMeasuredHeight();
    }
    
    /**
     * 获取指定位置缩略图的位置
     * 结果存储在{@link #mSrcImageLocation}
     */
    private void getSrcViewLocation(View view) {
        mSrcImageLocation[0] = 0;
        mSrcImageLocation[1] = 0;
        
        if (view == null) {
            return;
        }
        
        view.getLocationOnScreen(mSrcImageLocation);
        FragmentActivity activity = getActivity();
        if (activity != null && activity.getWindow() != null) {
            // 计算activity decorView的坐标，以此作为基准，计算缩略图在屏幕中的位置
            // 这样做的目的，一个是处理状态栏是否显示问题，如果显示状态栏，则getLocationOnScreen计算的高度要去除状态栏高度
            // 二是处理异形屏，在横屏状态下，异形屏耳朵区域可能黑屏显示，因此X轴坐标要去除黑屏区域宽度
            
            // 由于当前预览界面是全屏显示，不管状态栏是否显示，如果显示状态栏，则拖可绘制到状态栏之下，因此使用注释部分获取0，0反而导致位置错误
            // View contentView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            View contentView = activity.getWindow().getDecorView();
            if (contentView != null) {
                contentView.getLocationOnScreen(mIntTemp);
                mSrcImageLocation[0] -= mIntTemp[0];
                mSrcImageLocation[1] -= mIntTemp[1];
            }
        }
    }
    
    public void setPosition(int position/*当前预览图片的位置*/) {
        mPosition = position;
    }
    
    public interface OnExitListener {
        /**
         * 退出动作开始执行
         */
        void onStart();
        
        /**
         * 完全退出
         */
        void onExit();
    }
    
    public interface OnOpenListener {
        /**
         * 进入动画开始执行
         */
        void onStart();
        
        /**
         * 进入动画开始执行结束
         */
        void onEnd();
    }
    
}
