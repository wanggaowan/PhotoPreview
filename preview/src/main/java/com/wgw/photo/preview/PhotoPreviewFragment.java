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
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;

import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.github.chrisbanes.photoview.custom.PhotoViewAttacher;
import com.wgw.photo.preview.util.MatrixUtils;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.fragment.app.Fragment;
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
    
    /*
     此预览库动画采用androidx.transition.Transition库实现，使用此库有以下几个点需要注意
        说明：srcView 指定缩略图，且是ImageView类型
             helperView 指定实现从缩略图到预览图过度动画辅助类
             photoView 指定预览大图
        
        1. 如果srcView 的缩放类型为ScaleType.CENTER_CROP，那么helperView 设置的drawable 必须为photoView drawable，
           否则过度动画不能无缝衔接。比如以下情况：
           
           // srcView并非加载的原图，缩放类型为ScaleType.CENTER_CROP
           Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(srcView);
            
           // 此时不管photoView是否加载原图，如果helperView设置为srcView的drawable，那么过度动画不能无缝衔接
           Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(photoView);
            
        2. 如果srcView 的缩放类型为非ScaleType.CENTER_CROP，那么helperView 设置的drawable 必须为srcView drawable，
           否则过度动画不能无缝衔接。比如以下情况：
           
           // srcView缩放类型非ScaleType.CENTER_CROP
           Glide.with(mContext)
            .load(item)
            // 无论是否加载原图
            // .override(Target.SIZE_ORIGINAL)
            .into(srcView);
            
           // 此时不管photoView是否加载原图，如果helperView设置为photoView的drawable，那么过度动画不能无缝衔接
           Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(photoView);
           
        3. 由于存在srcView实际显示大小并非布局或代码指定的固定大小，因此在helperView外部包裹一层父布局，用于裁剪
     */
    
    private static final long OPEN_AND_EXIT_ANIM_DURATION = 200;
    private static final long OPEN_AND_EXIT_ANIM_DURATION_FOR_IMAGE = 350;
    
    private static final ArgbEvaluator ARGB_EVALUATOR = new ArgbEvaluator();
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    
    FrameLayout mRoot;
    private ProgressBar mLoading;
    private PhotoView mPhotoView;
    
    // 占位图，辅助执行过度动画
    // 为什么不直接使用mPhotoView进行过度动画，主要有一下几个问题
    // 1. 使用mPhotoView，某些情况下预览打开后图片某一部分自动被放大。导致这个的原因可能是使用Glide加载库，设置了placeholder导致
    // 2. 缩略图缩放类型非CENTER_CROP时，不能使用预览界面的图片进行过度，此时预览图需要设置为缩略图的drawable
    private ImageView mHelperView;
    private FrameLayout mHelperViewParent;
    
    private ShareData mShareData;
    private View mThumbnailView;
    private ScaleType mThumbnailViewScaleType;
    private int mPosition;
    // 当前界面是否需要执行动画
    private boolean mNeedInAnim;
    // 根据动画时间可决定整个预览是否需要执行动画
    private long mAnimDuration;
    
    private final int[] mIntTemp = new int[2];
    // 缩略图设定大小
    private final int[] mSrcViewSize = new int[2];
    // 缩略图实际界面显示大小
    private final int[] mSrcViewDrawSize = new int[2];
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
            mHelperViewParent = mRoot.findViewById(R.id.fl_parent);
            mPhotoView = mRoot.findViewById(R.id.photoView);
            mPhotoView.setPhotoPreviewFragment(this);
            mLoading = mRoot.findViewById(R.id.loading);
            initEvent();
        } else {
            mPhotoView.setVisibility(View.INVISIBLE);
            mPhotoView.setScaleLevels(
                PhotoViewAttacher.DEFAULT_MIN_SCALE,
                PhotoViewAttacher.DEFAULT_MID_SCALE,
                PhotoViewAttacher.DEFAULT_MAX_SCALE);
            
            mHelperViewParent.setTranslationX(0);
            mHelperViewParent.setTranslationY(0);
            setViewSize(mHelperViewParent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mHelperView.setVisibility(View.INVISIBLE);
            setViewSize(mHelperView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
        
        if (savedInstanceState == null) {
            // 防止预览时有些界面打开输入法，弹窗弹出时，输入法未及时关闭
            mRoot.setFocusableInTouchMode(true);
            mRoot.requestFocus();
            initData();
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
            if (mShareData != null && mShareData.config.onLongClickListener != null) {
                mShareData.config.onLongClickListener.onLongClick(mRoot);
            }
            return true;
        });
        
        mPhotoView.setOnClickListener(v -> exit());
    }
    
    private void initData() {
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
        
        mRoot.setBackgroundColor(Color.TRANSPARENT);
        mPhotoView.setStartView(mPosition == 0);
        mPhotoView.setEndView(mShareData.config.sources == null
            || mShareData.config.sources.size() == 0
            || mShareData.config.sources.size() - 1 == mPosition);
        
        mThumbnailView = getThumbnailView();
        if (mThumbnailView instanceof ImageView) {
            mThumbnailViewScaleType = ((ImageView) mThumbnailView).getScaleType();
        } else {
            mThumbnailViewScaleType = null;
        }
        
        mAnimDuration = getOpenAndExitAnimDuration(mThumbnailView);
        mNeedInAnim = mAnimDuration > 0 && mShareData.showNeedAnim && mPosition == mShareData.config.defaultShowPosition;
        
        loadData();
    }
    
    private void loadData() {
        initLoading();
        loadImage(mPhotoView);
        
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
        
        if (mThumbnailView == null) {
            enterAnimByScale();
            return;
        }
        
        enterAnimByTransition(mThumbnailView);
    }
    
    private void initNoAnim() {
        mRoot.setBackgroundColor(Color.BLACK);
        mHelperView.setVisibility(View.GONE);
        mPhotoView.setVisibility(View.VISIBLE);
    }
    
    private void enterAnimByScale() {
        mPhotoView.setMinimumScale(0);
        ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", 0, 1f);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(mAnimDuration);
        set.setInterpolator(INTERPOLATOR);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mPhotoView.setVisibility(View.VISIBLE);
                mHelperView.setVisibility(View.GONE);
                if (mShareData.onOpenListener != null) {
                    mShareData.onOpenListener.onStart();
                }
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                mPhotoView.setMinimumScale(PhotoViewAttacher.DEFAULT_MIN_SCALE);
                if (mShareData.onOpenListener != null) {
                    mShareData.onOpenListener.onEnd();
                }
            }
        });
        
        set.playTogether(scaleOa, getViewBgAnim(Color.BLACK, mAnimDuration, null));
        set.start();
    }
    
    /**
     * 使用Transition库实现过度动画
     */
    private void enterAnimByTransition(final View thumbnailView) {
        mPhotoView.post(() -> {
            mHelperView.setVisibility(View.VISIBLE);
            getSrcViewSize(thumbnailView);
            getSrcViewLocation(thumbnailView);
            float fromX = mSrcImageLocation[0];
            float fromY = mSrcImageLocation[1];
            
            mHelperViewParent.setTranslationX(fromX);
            mHelperViewParent.setTranslationY(fromY);
            setViewSize(mHelperViewParent, mSrcViewDrawSize[0], mSrcViewDrawSize[1]);
            
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1]);
            if (mThumbnailViewScaleType != null) {
                mHelperView.setScaleType(mThumbnailViewScaleType);
            }
            setHelperViewImage();
            
            mHelperView.post(() -> {
                TransitionSet transitionSet = new TransitionSet()
                    .setDuration(mAnimDuration)
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform())
                    .setInterpolator(INTERPOLATOR)
                    .addListener(new TransitionListenerAdapter() {
                        @Override
                        public void onTransitionStart(@NonNull Transition transition) {
                            doViewBgAnim(Color.BLACK, mAnimDuration, null);
                            if (mShareData.onOpenListener != null) {
                                mShareData.onOpenListener.onStart();
                            }
                        }
                        
                        @Override
                        public void onTransitionEnd(@NonNull Transition transition) {
                            mPhotoView.setVisibility(View.VISIBLE);
                            mHelperView.setVisibility(View.GONE);
                            if (mShareData.onOpenListener != null) {
                                mShareData.onOpenListener.onEnd();
                            }
                        }
                    });
                TransitionManager.beginDelayedTransition(mRoot, transitionSet);
                
                mHelperViewParent.setTranslationX(0);
                mHelperViewParent.setTranslationY(0);
                setViewSize(mHelperViewParent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                
                mHelperView.setScaleType(ScaleType.FIT_CENTER);
                setViewSize(mHelperView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            });
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
        mPhotoView.setOnMatrixChangeListener(this :: getPreviewDrawableSize);
        
        mPhotoView.setImageChangeListener(drawable -> {
            if (drawable != null) {
                mLoading.setVisibility(View.GONE);
                if (mAnimDuration <= 0) {
                    return;
                }
                
                if (!mNeedInAnim) {
                    // 当前界面未执行进入时动画，因此未初始化mIvAnim状态，此处初始化，用于退出时使用
                    setHelperViewImage();
                }
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
     * 设置辅助动画View的Image
     */
    private void setHelperViewImage() {
        Drawable drawable;
        if (mThumbnailViewScaleType == null || mThumbnailViewScaleType == ScaleType.CENTER_CROP) {
            drawable = mPhotoView.getDrawable();
        } else {
            Drawable drawable2 = ((ImageView) mThumbnailView).getDrawable();
            if (drawable2 == null) {
                drawable = mPhotoView.getDrawable();
            } else {
                drawable = drawable2;
            }
        }
        
        if (drawable instanceof GifDrawable) {
            loadImage(mHelperView);
        } else {
            mHelperView.setImageDrawable(drawable);
        }
    }
    
    /**
     * 获取预览图片真实大小，由于图片刚进入时，需要等待绘制，所以可能不能及时获取到准确的大小
     */
    private void getPreviewDrawableSize(RectF rectF) {
        if (mPhotoView.getScale() != 1) {
            return;
        }
        
        // 用于退出时计算移动后最终图像坐标使用
        // 刚设置图片就获取，此时可能获取不成功
        mNoScaleImageActualSize[0] = rectF.width();
        mNoScaleImageActualSize[1] = rectF.height();
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
        
        mPhotoView.setMinimumScale(0);
        if (mThumbnailView == null) {
            ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", mPhotoView.getScale(), 0f);
            AnimatorSet set = new AnimatorSet();
            set.setDuration(mAnimDuration);
            set.setInterpolator(INTERPOLATOR);
            set.playTogether(scaleOa, getViewBgAnim(Color.TRANSPARENT, mAnimDuration, null));
            
            set.addListener(new AnimatorListenerAdapter() {
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
            float x = mRoot.getWidth() / 2f - mNoScaleImageActualSize[0] / 2 // 预览图片未移动未缩放时实际绘制drawable左上角Z轴值
                - mPhotoView.getScrollX() // 向左或向右移动的距离
                + imageActualSize[0] * (1 - mPhotoView.getScale() - errorRatio); // 由于在向下移动时，伴随图片缩小，因此需要加上缩小宽度
            
            mHelperViewParent.setTranslationX(x);
            mHelperViewParent.setTranslationY(y);
            setViewSize(mHelperViewParent, ((int) imageActualSize[0]), ((int) imageActualSize[1]));
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
                .setInterpolator(INTERPOLATOR)
                .addListener(new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(@NonNull Transition transition) {
                        if (mShareData.onExitListener != null) {
                            mShareData.onExitListener.onExit();
                        }
                    }
                    
                    @Override
                    public void onTransitionStart(@NonNull Transition transition) {
                        doViewBgAnim(Color.TRANSPARENT, mAnimDuration, null);
                    }
                });
            
            TransitionManager.beginDelayedTransition(mRoot, transitionSet);
            
            mHelperViewParent.setTranslationX(mSrcImageLocation[0]);
            mHelperViewParent.setTranslationY(mSrcImageLocation[1]);
            setViewSize(mHelperViewParent, mSrcViewDrawSize[0], mSrcViewDrawSize[1]);
            
            if (mThumbnailViewScaleType != null) {
                mHelperView.setScaleType(mThumbnailViewScaleType);
            }
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1]);
        });
    }
    
    void doViewBgAnim(final int endColor, long duration, AnimatorListenerAdapter listenerAdapter) {
        getViewBgAnim(endColor, duration, listenerAdapter).start();
    }
    
    Animator getViewBgAnim(final int endColor, long duration, AnimatorListenerAdapter listenerAdapter) {
        final int start = ((ColorDrawable) mRoot.getBackground()).getColor();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.addUpdateListener(animation ->
            mRoot.setBackgroundColor((int) ARGB_EVALUATOR.evaluate(animation.getAnimatedFraction(), start, endColor)));
        animator.setDuration(duration);
        animator.setInterpolator(INTERPOLATOR);
        if (listenerAdapter != null) {
            animator.addListener(listenerAdapter);
        }
        return animator;
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
     * 结果存储在{@link #mSrcViewSize}
     */
    private void getSrcViewSize(View view) {
        mSrcViewSize[0] = 0;
        mSrcViewSize[1] = 0;
        mSrcViewDrawSize[0] = 0;
        mSrcViewDrawSize[0] = 0;
        
        if (view == null) {
            return;
        }
        
        mSrcViewSize[0] = view.getWidth();
        mSrcViewSize[1] = view.getHeight();
        
        mSrcViewDrawSize[0] = mSrcViewSize[0];
        mSrcViewDrawSize[1] = mSrcViewSize[1];
        getSrcViewDrawSize(view.getParent());
    }
    
    /**
     * 获取父类最小宽高，可能View设置了固定宽高，但是父类被裁剪，因此实际绘制区域可能没有设定宽高那么大
     */
    private void getSrcViewDrawSize(ViewParent parent) {
        if (parent instanceof View) {
            int width = ((View) parent).getWidth();
            if (width < mSrcViewDrawSize[0]) {
                mSrcViewDrawSize[0] = width;
            }
            
            int height = ((View) parent).getHeight();
            if (height < mSrcViewDrawSize[1]) {
                mSrcViewDrawSize[1] = height;
            }
            
            if (width > mSrcViewDrawSize[0] && height > mSrcViewDrawSize[1]) {
                return;
            }
            
            Log.e("xxx", "src parent size:" + width + ";" + height + ";" + parent.toString());
            Log.e("xxx", "srcViewDrawSize size:" + Arrays.toString(mIntTemp));
            getSrcViewDrawSize(parent.getParent());
        }
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
        // 预览界面采用沉浸式全屏显示模式，如果手机系统支持，横竖屏都绘制到耳朵区域
        // 以下逻辑防止部分手机横屏时，耳朵区域不显示内容，此时设置的预览坐标不能采用OnScreen坐标
        mRoot.getLocationOnScreen(mIntTemp);
        mSrcImageLocation[0] -= mIntTemp[0];
        mSrcImageLocation[1] -= mIntTemp[1];
    }
    
    /**
     * 设置当前预览图片的位置
     */
    public void setPosition(int position) {
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
