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
import com.wgw.photo.preview.interfaces.OnLongClickListener;
import com.wgw.photo.preview.util.MatrixUtils;

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
        说明：适配不同缩放类型的逻辑都是基于Glide图片加载库
             srcView 指定缩略图，且是ImageView类型
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
    // 2. 预览动画开始时，需要对图片进行位移，裁减等操作，而预览大图加载时，不能调整其大小和缩放类型，否则预览大图显示将出现问题
    private ImageView mHelperView;
    private FrameLayout mHelperViewParent;
    // 辅助加载，主要针对加载大图且指定缩放类型，加载期间不能更改
    private PreloadImageView mHelperPreLoad;
    
    @Nullable
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
    // 缩略图实际可显示大小
    private final int[] mSrcViewParentSize = new int[2];
    private final int[] mSrcImageLocation = new int[2];
    private final float[] mFloatTemp = new float[2];
    // 记录预览界面图片缩放倍率为1时图片真实绘制大小
    private final float[] mNoScaleImageActualSize = new float[2];
    // 打开预览Transition动画是否已开始
    private boolean mEnterAnimByTransitionStart = false;
    // 加载预览图时，总共加载次数
    private int mImageLoadCount;
    // 辅助视图是否单独加载图片而不是依赖mPhotoView
    private boolean mHelpViewSelfLoadImage;
    
    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRoot == null) {
            mRoot = (FrameLayout) inflater.inflate(R.layout.fragment_preview, null);
            mHelperView = mRoot.findViewById(R.id.iv_anim);
            mHelperViewParent = mRoot.findViewById(R.id.fl_parent);
            mHelperPreLoad = mRoot.findViewById(R.id.iv_pre_load);
            mHelperPreLoad.setActualView(mHelperView);
            mPhotoView = mRoot.findViewById(R.id.photoView);
            mPhotoView.setPhotoPreviewFragment(this);
            mLoading = mRoot.findViewById(R.id.loading);
            initEvent();
        } else {
            mHelperViewParent.setVisibility(View.INVISIBLE);
            mPhotoView.setVisibility(View.INVISIBLE);
            mPhotoView.setScaleLevels(
                PhotoViewAttacher.DEFAULT_MIN_SCALE,
                PhotoViewAttacher.DEFAULT_MID_SCALE,
                PhotoViewAttacher.DEFAULT_MAX_SCALE);
            
            mHelperViewParent.setTranslationX(0);
            mHelperViewParent.setTranslationY(0);
            setViewSize(mHelperViewParent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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
            if (keyCode == KeyEvent.KEYCODE_BACK
                && (event == null || event.getAction() == KeyEvent.ACTION_UP)
                && mPhotoView.getVisibility() == View.VISIBLE) {
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
            if (mShareData != null) {
                OnLongClickListener listener = mShareData.config.onLongClickListener;
                if (listener != null) {
                    listener.onLongClick(mRoot);
                }
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
        mRoot.setBackgroundColor(Color.TRANSPARENT);
        mPhotoView.setStartView(mPosition == 0);
        mPhotoView.setEndView(mShareData.config.sources == null
            || mShareData.config.sources.size() == 0
            || mShareData.config.sources.size() - 1 == mPosition);
        
        mThumbnailView = getThumbnailView(mShareData);
        mAnimDuration = getOpenAndExitAnimDuration(mThumbnailView, mShareData);
        mNeedInAnim = mAnimDuration > 0 && mShareData.showNeedAnim && mPosition == mShareData.config.defaultShowPosition;
        initThumbnailViewScaleType();
        
        initLoading(mShareData);
        loadData(mShareData);
    }
    
    /**
     * 初始化缩略图的缩放类型
     */
    private void initThumbnailViewScaleType() {
        if (mThumbnailView instanceof ImageView) {
            mThumbnailViewScaleType = ((ImageView) mThumbnailView).getScaleType();
            if (mThumbnailViewScaleType == ScaleType.CENTER || mThumbnailViewScaleType == ScaleType.CENTER_INSIDE) {
                Drawable drawable = ((ImageView) mThumbnailView).getDrawable();
                if (drawable != null && drawable.getIntrinsicWidth() >= mThumbnailView.getWidth()
                    && drawable.getIntrinsicHeight() >= mThumbnailView.getHeight()) {
                    // ScaleType.CENTER：不缩放图片，如果图片大于图片控件，效果与ScaleType.CENTER_CROP一致
                    if (mThumbnailViewScaleType == ScaleType.CENTER) {
                        mThumbnailViewScaleType = ScaleType.CENTER_CROP;
                    }
                } else if (mAnimDuration > 0) {
                    // 当图片小于控件大小时，对于缩放类型ScaleType.CENTER 和 ScaleType.CENTER_INSIDE，需要以动画的方式打开
                    // 除非整个预览不需要动画，否则再推出时，可能不会无缝衔接
                    mNeedInAnim = true;
                }
            }
        } else {
            mThumbnailViewScaleType = null;
        }
    }
    
    private void loadData(ShareData shareData) {
        loadImage(mPhotoView);
        
        if (!mNeedInAnim) {
            initNoAnim();
            // 整个预览无需执行动画，因此第一个执行的预览界面执行一次预览打开回调
            if (shareData.config.defaultShowPosition == mPosition) {
                callOnOpen(null);
                shareData.showNeedAnim = false;
            }
            return;
        }
        
        // 处理进入时的动画
        mNeedInAnim = false;
        shareData.showNeedAnim = false;
        
        if (mThumbnailView == null) {
            enterAnimByScale();
            return;
        }
        
        enterAnimByTransition(mThumbnailView, shareData);
    }
    
    private void initNoAnim() {
        mRoot.setBackgroundColor(Color.BLACK);
        mHelperViewParent.setVisibility(View.INVISIBLE);
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
                mHelperViewParent.setVisibility(View.INVISIBLE);
                callOnOpen(true);
            }
            
            @Override
            public void onAnimationEnd(Animator animation) {
                mPhotoView.setMinimumScale(PhotoViewAttacher.DEFAULT_MIN_SCALE);
                callOnOpen(false);
            }
        });
        
        set.playTogether(scaleOa, getViewBgAnim(Color.BLACK, mAnimDuration, null));
        set.start();
    }
    
    /**
     * 使用Transition库实现过度动画
     */
    private void enterAnimByTransition(final View thumbnailView, final ShareData shareData) {
        mHelperViewParent.setVisibility(View.INVISIBLE);
        long delay = mShareData.openAnimDelayTime;
        if (delay > 0) {
            mHelperView.postDelayed(() -> checkPreviewImageLoad(0), delay);
            initEnterAnimByTransition(thumbnailView);
        } else {
            long enterAnimDelay = getEnterAnimDelay();
            if (enterAnimDelay == 0) {
                initEnterAnimByTransition(thumbnailView);
                doEnterAnimByTransition(shareData);
            } else {
                mHelperView.postDelayed(() -> checkPreviewImageLoad(enterAnimDelay), enterAnimDelay);
                initEnterAnimByTransition(thumbnailView);
            }
        }
    }
    
    /**
     * 初始化Transition所需内容
     */
    private void initEnterAnimByTransition(final View thumbnailView) {
        getSrcViewSize(thumbnailView);
        getSrcViewLocation(thumbnailView);
        
        mHelperViewParent.setTranslationX(mSrcImageLocation[0]);
        mHelperViewParent.setTranslationY(mSrcImageLocation[1]);
        setViewSize(mHelperViewParent, mSrcViewParentSize[0], mSrcViewParentSize[1]);
        
        setHelperViewDataByThumbnail();
    }
    
    /**
     * 根据缩列图数据设置预览占位View大小、位置和缩放模式
     */
    private void setHelperViewDataByThumbnail() {
        if (mThumbnailViewScaleType != null) {
            mHelperView.setScaleType(mThumbnailViewScaleType);
        } else {
            mHelperView.setScaleType(ScaleType.FIT_CENTER);
        }
        
        mHelperView.setTranslationX(0);
        mHelperView.setTranslationY(0);
        if (mShareData == null || mShareData.config.shapeTransformType == null || mThumbnailViewScaleType == null) {
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1]);
            return;
        }
        
        // 需要进行图片裁剪，部分缩放类型不能使图片充满控件，而裁剪基于控件，因此设置控件大小与图片大小一致
        Drawable drawable = ((ImageView) mThumbnailView).getDrawable();
        if (drawable == null) {
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1]);
            return;
        }
        
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1]);
            return;
        }
        
        int width = mSrcViewSize[0];
        int height = mSrcViewSize[1];
        if (mShareData.config.shapeTransformType == ShapeTransformType.CIRCLE) {
            int minSize = Math.min(width, height);
            switch (mThumbnailViewScaleType) {
                case FIT_START:
                case MATRIX:
                    width = minSize;
                    height = minSize;
                    break;
                
                case FIT_END:
                    mHelperView.setTranslationX(width - minSize);
                    width = minSize;
                    mHelperView.setTranslationY(height - minSize);
                    height = minSize;
                    break;
                
                case FIT_CENTER:
                case CENTER_INSIDE:
                case CENTER:
                    mHelperView.setTranslationX((width - minSize) / 2f);
                    width = minSize;
                    mHelperView.setTranslationY((height - minSize) / 2f);
                    height = minSize;
                    break;
            }
            
            // 需要重新设置缩放类型为CENTER_CROP，否则动画不会无缝衔接
            // 重新调整缩放类型，全部都是为了适配 Transition动画ChangeImageTransform过渡
            mHelperView.setScaleType(ScaleType.CENTER_CROP);
            setViewSize(mHelperView, width, height);
            return;
        }
        
        switch (mThumbnailViewScaleType) {
            case FIT_START:
            case MATRIX:
                if (intrinsicWidth < width) {
                    width = intrinsicWidth;
                }
                
                if (intrinsicHeight < height) {
                    height = intrinsicHeight;
                }
                mHelperView.setScaleType(ScaleType.FIT_START);
                break;
            
            case FIT_END:
                if (intrinsicWidth < width) {
                    mHelperView.setTranslationX(width - intrinsicWidth);
                    width = intrinsicWidth;
                }
                
                if (intrinsicHeight < height) {
                    mHelperView.setTranslationY(height - intrinsicHeight);
                    height = intrinsicHeight;
                }
                break;
            
            case FIT_XY:
                break;
            
            default:
                if (intrinsicWidth < width) {
                    mHelperView.setTranslationX((width - intrinsicWidth) / 2f);
                    width = intrinsicWidth;
                }
                
                if (intrinsicHeight < height) {
                    mHelperView.setTranslationY((height - intrinsicHeight) / 2f);
                    height = intrinsicHeight;
                }
                
                // 需要重新设置缩放类型为CENTER_CROP，否则动画不会无缝衔接
                // 重新调整缩放类型，全部都是为了适配 Transition动画ChangeImageTransform过渡
                mHelperView.setScaleType(ScaleType.CENTER_CROP);
                break;
        }
        
        setViewSize(mHelperView, width, height);
    }
    
    /**
     * 检查预览图片加载情况
     */
    private void checkPreviewImageLoad(long totalDelay) {
        if (mShareData == null) {
            enterAnimByScale();
            return;
        }
        
        if (totalDelay > 200) {
            doEnterAnimByTransition(mShareData);
            return;
        }
        
        long delay = getEnterAnimDelay();
        if (delay == 0) {
            doEnterAnimByTransition(mShareData);
            return;
        }
        
        mHelperView.postDelayed(() -> checkPreviewImageLoad(delay + totalDelay), delay);
    }
    
    private void doEnterAnimByTransition(ShareData shareData) {
        TransitionSet transitionSet = new TransitionSet()
            .setDuration(mAnimDuration)
            .addTransition(new ChangeBounds())
            .addTransition(new ChangeTransform())
            .setInterpolator(INTERPOLATOR)
            .addListener(new TransitionListenerAdapter() {
                @Override
                public void onTransitionStart(@NonNull Transition transition) {
                    mHelperViewParent.setVisibility(View.VISIBLE);
                    doViewBgAnim(Color.BLACK, mAnimDuration, null);
                    callOnOpen(true);
                }
                
                @Override
                public void onTransitionEnd(@NonNull Transition transition) {
                    mEnterAnimByTransitionStart = false;
                    mPhotoView.setVisibility(View.VISIBLE);
                    mHelperViewParent.setVisibility(View.INVISIBLE);
                    setHelperViewImage();
                    callOnOpen(false);
                }
            });
        
        
        if (shareData.config.shapeTransformType != null) {
            if (shareData.config.shapeTransformType == ShapeTransformType.CIRCLE) {
                transitionSet.addTransition(
                    new ChangeShape(Math.min(mSrcViewSize[0], mSrcViewSize[1]) / 2f, 0)
                        .addTarget(mHelperView));
            } else {
                transitionSet.addTransition(
                    new ChangeShape(shareData.config.shapeCornerRadius, 0)
                        .addTarget(mHelperView));
            }
        }
        
        if (getEnterAnimDelay() == 0) {
            // ChangeImageTransform执行MATRIX变换，因此一定需要最终加载完成图片
            transitionSet.addTransition(new ChangeImageTransform().addTarget(mHelperView));
        }
        
        mEnterAnimByTransitionStart = true;
        TransitionManager.beginDelayedTransition((ViewGroup) mHelperViewParent.getParent(), transitionSet);
        
        mHelperViewParent.setTranslationX(0);
        mHelperViewParent.setTranslationY(0);
        setViewSize(mHelperViewParent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        
        mHelperView.setTranslationX(0);
        mHelperView.setTranslationY(0);
        mHelperView.setScaleType(ScaleType.FIT_CENTER);
        setViewSize(mHelperView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }
    
    /**
     * 使用Transition库实现动画时，检测是否需要延迟，主要是缩放类型为ScaleType.CENTER_CROP时，等待最终图像的获取
     */
    private long getEnterAnimDelay() {
        Drawable drawable = mHelperView.getDrawable();
        if (drawable == null) {
            return 50;
        }
        
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            return 50;
        }
        
        if (mImageLoadCount > 1) {
            Drawable drawable2 = ((ImageView) mThumbnailView).getDrawable();
            if (drawable2 != null && drawable2.getIntrinsicWidth() >= drawable.getIntrinsicWidth()
                && drawable2.getIntrinsicHeight() >= drawable.getIntrinsicHeight()) {
                return 50;
            }
        }
        
        return 0;
    }
    
    /**
     * 加载图片
     */
    private void loadImage(ImageView imageView) {
        if (mShareData != null && mShareData.config.imageLoader != null) {
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
    private void initLoading(ShareData shareData) {
        mPhotoView.setOnMatrixChangeListener(this :: getPreviewDrawableSize);
        
        mPhotoView.setImageChangeListener(drawable -> {
            if (drawable != null) {
                mImageLoadCount++;
                mLoading.setVisibility(View.GONE);
                if (mAnimDuration <= 0) {
                    return;
                }
                
                // 当前界面未执行进入时动画，因此未初始化mIvAnim状态，此处初始化，用于退出时使用
                setHelperViewImage();
            }
        });
        
        if (shareData.config.delayShowProgressTime < 0) {
            mLoading.setVisibility(View.GONE);
            return;
        }
        
        if (shareData.config.progressDrawable != null) {
            mLoading.setIndeterminateDrawable(shareData.config.progressDrawable);
        }
        
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && shareData.config.progressColor != null) {
            mLoading.setIndeterminateTintList(ColorStateList.valueOf(shareData.config.progressColor));
        }
        
        mLoading.setVisibility(shareData.config.delayShowProgressTime == 0 ? View.VISIBLE : View.GONE);
        if (shareData.config.defaultShowPosition > 0) {
            // 监听指定延迟后图片是否加载成功
            mPhotoView.postDelayed(() -> {
                if (mPhotoView.getDrawable() == null) {
                    mLoading.setVisibility(View.VISIBLE);
                }
            }, shareData.config.delayShowProgressTime);
        }
    }
    
    /**
     * 设置辅助动画View的Image
     */
    private void setHelperViewImage() {
        if (mEnterAnimByTransitionStart || mHelpViewSelfLoadImage) {
            return;
        }
        
        Drawable drawable = mPhotoView.getDrawable();
        if (drawable instanceof GifDrawable
            // 不切圆形时，ScaleType.MATRIX需要单独加载图片，因为无法使用其它缩放类型达到ScaleType.MATRIX绘制效果
            || (mThumbnailViewScaleType == ScaleType.MATRIX
            && (mShareData == null || mShareData.config.shapeTransformType == null
            || mShareData.config.shapeTransformType != ShapeTransformType.CIRCLE))) {
            mHelpViewSelfLoadImage = true;
            if (mThumbnailViewScaleType == ScaleType.MATRIX) {
                mHelperPreLoad.setScaleType(ScaleType.MATRIX);
            } else {
                mHelperPreLoad.setScaleType(ScaleType.FIT_CENTER);
            }
            loadImage(mHelperPreLoad);
        } else {
            mHelperPreLoad.setImageDrawable(drawable);
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
            callOnExit(null);
            return true;
        }
        
        if (mPhotoView.getDrawable() == null) {
            callOnExit(true);
            doViewBgAnim(Color.TRANSPARENT, mAnimDuration, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    callOnExit(false);
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
                    callOnExit(false);
                }
            });
            
            callOnExit(true);
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
        if (mThumbnailViewScaleType == ScaleType.MATRIX || mPhotoView.getScale() != 1) {
            // thumbnailView是ScaleType.MATRIX需要设置ImageView与drawable大小一致,且如果是下拉后缩小后关闭预览，则肯能无法与缩略图无缝衔接
            // 得到关闭时预览图片真实绘制大小
            float[] imageActualSize = mFloatTemp;
            getImageActualSize(mPhotoView, imageActualSize);
            if (mNoScaleImageActualSize[0] == 0 && mNoScaleImageActualSize[1] == 0) {
                // 此时只是降低偏移值，但是这是不准确的
                getImageActualSize(mHelperView, mNoScaleImageActualSize);
            }
            
            if (mPhotoView.getScale() < 1 || (mThumbnailViewScaleType == ScaleType.MATRIX && mPhotoView.getScale() == 1)) {
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
            } else if (mPhotoView.getScale() > 1) {
                Matrix imageMatrix = mPhotoView.getImageMatrix();
                float scrollX = MatrixUtils.getValue(imageMatrix, Matrix.MTRANS_X);
                float scrollY = MatrixUtils.getValue(imageMatrix, Matrix.MTRANS_Y);
                float y = imageActualSize[1] > mRoot.getHeight() ? scrollY : mRoot.getHeight() / 2f - imageActualSize[1] / 2f;
                float x = imageActualSize[0] > mRoot.getWidth() ? scrollX : mRoot.getWidth() / 2f - imageActualSize[0] / 2f;
                
                mHelperViewParent.setTranslationX(x);
                mHelperViewParent.setTranslationY(y);
            }
            setViewSize(mHelperViewParent, ((int) imageActualSize[0]), ((int) imageActualSize[1]));
            setViewSize(mHelperView, ((int) imageActualSize[0]), ((int) imageActualSize[1]));
        }
        
        mHelperView.setScaleType(ScaleType.FIT_CENTER);
        getSrcViewSize(thumbnailView);
        getSrcViewLocation(thumbnailView);
        callOnExit(true);
        
        mHelperView.post(() -> {
            TransitionSet transitionSet = new TransitionSet()
                .setDuration(mAnimDuration)
                .addTransition(new ChangeBounds())
                .addTransition(new ChangeTransform())
                .addTransition(new ChangeImageTransform().addTarget(mHelperView))
                .setInterpolator(INTERPOLATOR)
                .addListener(new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(@NonNull Transition transition) {
                        callOnExit(false);
                    }
                    
                    @Override
                    public void onTransitionStart(@NonNull Transition transition) {
                        mPhotoView.setVisibility(View.INVISIBLE);
                        mHelperViewParent.setVisibility(View.VISIBLE);
                        doViewBgAnim(Color.TRANSPARENT, mAnimDuration, null);
                    }
                });
            
            if (mShareData != null && mShareData.config.shapeTransformType != null) {
                if (mShareData.config.shapeTransformType == ShapeTransformType.CIRCLE) {
                    transitionSet.addTransition(
                        new ChangeShape(0, Math.min(mSrcViewSize[0], mSrcViewSize[1]) / 2f)
                            .addTarget(mHelperView));
                } else {
                    transitionSet.addTransition(
                        new ChangeShape(0, mShareData.config.shapeCornerRadius)
                            .addTarget(mHelperView));
                }
            }
            
            TransitionManager.beginDelayedTransition((ViewGroup) mHelperViewParent.getParent(), transitionSet);
            
            mHelperViewParent.setTranslationX(mSrcImageLocation[0]);
            mHelperViewParent.setTranslationY(mSrcImageLocation[1]);
            setViewSize(mHelperViewParent, mSrcViewParentSize[0], mSrcViewParentSize[1]);
            
            setHelperViewDataByThumbnail();
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
    private long getOpenAndExitAnimDuration(View thumbnailView, ShareData shareData) {
        if (shareData.config.animDuration != null) {
            return shareData.config.animDuration;
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
    private View getThumbnailView(ShareData shareData) {
        View view = getThumbnailViewNotDefault(shareData, mPosition);
        if (view == null) {
            if (mPosition != shareData.config.defaultShowPosition) {
                return getThumbnailViewNotDefault(shareData, shareData.config.defaultShowPosition);
            }
        }
        return view;
    }
    
    /**
     * 获取指定位置的缩略图
     */
    @Nullable
    private View getThumbnailViewNotDefault(ShareData shareData, int position) {
        if (mThumbnailView != null) {
            return mThumbnailView;
        }
        
        if (shareData.thumbnailView != null) {
            return shareData.thumbnailView;
        } else if (shareData.findThumbnailView != null) {
            return shareData.findThumbnailView.findView(position);
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
        mSrcViewParentSize[0] = 0;
        mSrcViewParentSize[1] = 0;
        
        if (view == null) {
            return;
        }
        
        mSrcViewSize[0] = view.getWidth();
        mSrcViewSize[1] = view.getHeight();
        
        mSrcViewParentSize[0] = mSrcViewSize[0];
        mSrcViewParentSize[1] = mSrcViewSize[1];
        getSrcViewParentSize(view.getParent());
    }
    
    /**
     * 获取父类最小宽高，可能View设置了固定宽高，但是父类被裁剪，因此实际绘制区域可能没有设定宽高那么大
     */
    private void getSrcViewParentSize(ViewParent parent) {
        if (parent instanceof View) {
            int width = ((View) parent).getWidth();
            if (width < mSrcViewParentSize[0]) {
                mSrcViewParentSize[0] = width;
            }
            
            int height = ((View) parent).getHeight();
            if (height < mSrcViewParentSize[1]) {
                mSrcViewParentSize[1] = height;
            }
            
            if (width > mSrcViewParentSize[0] && height > mSrcViewParentSize[1]) {
                return;
            }
            
            getSrcViewParentSize(parent.getParent());
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
     * 预览界面打开时执行回调
     */
    private void callOnOpen(Boolean start) {
        if (mShareData != null) {
            OnOpenListener onOpenListener = mShareData.onOpenListener;
            if (onOpenListener != null) {
                if (start == null) {
                    onOpenListener.onStart();
                    onOpenListener.onEnd();
                } else if (start) {
                    onOpenListener.onStart();
                } else {
                    onOpenListener.onEnd();
                }
            }
        }
    }
    
    /**
     * 预览界面关闭时执行回调
     */
    private void callOnExit(Boolean start) {
        if (mShareData != null) {
            OnExitListener onExitListener = mShareData.onExitListener;
            if (onExitListener != null) {
                if (start == null) {
                    onExitListener.onStart();
                    onExitListener.onExit();
                } else if (start) {
                    onExitListener.onStart();
                } else {
                    onExitListener.onExit();
                }
            }
        }
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
