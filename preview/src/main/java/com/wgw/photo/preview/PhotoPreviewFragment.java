package com.wgw.photo.preview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle.State;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnLongClickListener;
import com.wgw.photo.preview.photoview.OnFingerUpListener;
import com.wgw.photo.preview.photoview.OnViewDragListener;
import com.wgw.photo.preview.photoview.PhotoView;
import com.wgw.photo.preview.util.Utils;
import com.wgw.photo.preview.util.notch.OSUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 图片预览Fragment
 *
 * @author Created by 汪高皖 on 2019/2/27 0027 11:24
 */
public class PhotoPreviewFragment extends Fragment {
    private static final long OPEN_AND_EXIT_ANIM_DURATION = 200;
    
    private static final long OPEN_AND_EXIT_ANIM_DURATION_ORIENTATION = 250;
    
    private Context mContext;
    private FrameLayout mRoot;
    // 用于计算0,0位置视图在界面的位置
    private View mZeroLocationView;
    private PhotoView mPhotoView;
    private ProgressBar mLoading;
    
    private ScheduledExecutorService mService;
    private ScheduledFuture<?> mSchedule;
    private Handler mHandler;
    
    private ImageLoader mLoadImage;
    private View mSrcImageContainer;
    private int mDefaultShowPosition;
    private int mPosition;
    private Object mUrl;
    private final int[] mImageSize = new int[2];
    private final int[] mImageLocationTemp = new int[2];
    private final int[] mImageLocation = new int[2];
    private boolean mNeedInAnim;
    private long mDelayShowProgressTime;
    private Integer mProgressColor;
    private Drawable mProgressDrawable;
    private boolean mFullScreen;
    
    private OnExitListener mOnExitListener;
    private OnLongClickListener mOnLongClickListener;
    
    // 透明度
    private float mAlpha = 1f;
    private int mIntAlpha = 255;
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            initData();
        }
    }
    
    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;
        if (savedInstanceState == null) {
            view = inflater.inflate(R.layout.fragment_preview, null);
            mRoot = view.findViewById(R.id.root);
            mZeroLocationView = view.findViewById(R.id.v_location);
            mRoot.setFocusableInTouchMode(true);
            mRoot.requestFocus();
            
            mPhotoView = view.findViewById(R.id.photoView);
            mLoading = view.findViewById(R.id.loading);
            
            initEvent();
            onLoadData();
        }
        return view;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSchedule != null) {
            mSchedule.cancel(true);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.shutdownNow();
        }
        
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
    
    private void initData() {
        mService = Executors.newScheduledThreadPool(1);
        mHandler = new Handler();
    }
    
    private void initEvent() {
        mRoot.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    exit();
                    return true;
                }
                return false;
            }
        });
        
        mPhotoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnLongClickListener != null) {
                    mOnLongClickListener.onLongClick(mRoot);
                }
                return true;
            }
        });
        
        mPhotoView.getAttacher().setOnFingerUpListener(new OnFingerUpListener() {
            @Override
            public void onFingerUp() {
                // 这里恢复位置和透明度
                if (mIntAlpha < 200 && mOnExitListener != null) {
                    exit();
                } else {
                    ValueAnimator va = ValueAnimator.ofFloat(mPhotoView.getAlpha(), 1f);
                    ValueAnimator bgVa = ValueAnimator.ofInt(mIntAlpha, 255);
                    va.setDuration(200);
                    bgVa.setDuration(200);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mPhotoView.setAlpha((Float) animation.getAnimatedValue());
                        }
                    });
                    
                    bgVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mRoot.getBackground().setAlpha((Integer) animation.getAnimatedValue());
                        }
                    });
                    va.start();
                    bgVa.start();
                    mPhotoView.smoothResetPosition();
                }
                
                mAlpha = 1f;
                mIntAlpha = 255;
            }
        });
        
        mPhotoView.setOnViewDragListener(new OnViewDragListener() {
            @Override
            public void onDrag(float dx, float dy) {
                // 移动图像
                mPhotoView.scrollBy(((int) -dx), ((int) -dy));
                float scrollY = mPhotoView.getScrollY();
                if (scrollY >= 0) {
                    mAlpha = 1f;
                    mIntAlpha = 255;
                } else {
                    mAlpha -= dy * 0.001f;
                    mIntAlpha -= dy * 0.25;
                }
                
                if (mAlpha > 1) {
                    mAlpha = 1f;
                } else if (mAlpha < 0) {
                    mAlpha = 0f;
                }
                if (mIntAlpha < 0) {
                    mIntAlpha = 0;
                } else if (mIntAlpha > 255) {
                    mIntAlpha = 255;
                }
                // 更改透明度
                mRoot.getBackground().setAlpha(mIntAlpha);
                if (scrollY < 0 && mAlpha >= 0.6) {
                    // 更改大小
                    mPhotoView.getAttacher().setScale(mAlpha);
                }
            }
        });
        
        
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
        
        mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
    }
    
    public void onLoadData() {
        mAlpha = 1f;
        mIntAlpha = 255;
        
        mPhotoView.setVisibility(View.INVISIBLE);
        mPhotoView.setImageDrawable(null);
        mLoadImage.onLoadImage(mPosition, mUrl, mPhotoView);
        checkLoadResult();
        
        mRoot.getBackground().setAlpha(mIntAlpha);
        if (mNeedInAnim) {
            mNeedInAnim = false;
            // 添加点击进入时的动画
            mPhotoView.post(new Runnable() {
                @Override
                public void run() {
                    mPhotoView.setVisibility(View.VISIBLE);
                    getSrcViewSize(mPosition);
                    float scaleX = mPhotoView.getWidth() == 0 ? 0 : mImageSize[0] * 1f / mPhotoView.getWidth();
                    float scaleY = mPhotoView.getHeight() == 0 ? 0 : mImageSize[1] * 1f / mPhotoView.getHeight();
                    float scale = 0;
                    if (scaleX > 0 && scaleY > 0) {
                        Matrix m = new Matrix();
                        m.postScale(scaleX, scaleY);
                        scale = mPhotoView.getAttacher().getScale(m);
                    }
                    
                    getSrcViewLocation(mPosition);
                    ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", scale, 1f);
                    ObjectAnimator xOa = ObjectAnimator.ofFloat(mPhotoView, "translationX",
                        mImageLocation[0] - mPhotoView.getWidth() / 2f, 0f);
                    ObjectAnimator yOa = ObjectAnimator.ofFloat(mPhotoView, "translationY",
                        getTranslationY(mImageLocation[1]), 0f);
                    
                    AnimatorSet set = new AnimatorSet();
                    set.setDuration(getOpenAndExitAnimDuration());
                    set.playTogether(scaleOa, xOa, yOa);
                    set.start();
                }
            });
        } else {
            mPhotoView.setVisibility(View.VISIBLE);
        }
    }
    
    private void checkLoadResult() {
        // 循环查看是否添加上了图片
        if (mDelayShowProgressTime < 0) {
            mLoading.setVisibility(View.GONE);
            return;
        }
        
        if (mProgressDrawable != null) {
            mLoading.setIndeterminateDrawable(mProgressDrawable);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mProgressColor != null) {
            mLoading.setIndeterminateTintList(ColorStateList.valueOf(mProgressColor));
        }
        
        mLoading.setVisibility(mDelayShowProgressTime == 0 ? View.VISIBLE : View.GONE);
        mSchedule = mService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (mPhotoView.getDrawable() != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mLoading.setVisibility(View.GONE);
                        }
                    });
                    mSchedule.cancel(true);
                } else if (mLoading.getVisibility() == View.GONE) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mLoading.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }, mDelayShowProgressTime == 0 ? 100 : mDelayShowProgressTime, 100, TimeUnit.MILLISECONDS);
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
        
        float[] imageAcSize = getImageActualSize(mPhotoView);
        float scale = 0;
        if (imageAcSize[0] > 0 && imageAcSize[1] > 0) {
            Matrix m = new Matrix();
            getSrcViewSize(mPosition);
            m.postScale((mImageSize[0] * 1f / imageAcSize[0]), (mImageSize[1] * 1f / imageAcSize[1]));
            scale = mPhotoView.getAttacher().getScale(m);
        }
        
        getSrcViewLocation(mPosition);
        ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", scale * mPhotoView.getScale());
        ObjectAnimator xOa = ObjectAnimator.ofFloat(mPhotoView, "translationX",
            mImageLocation[0] - mPhotoView.getWidth() / 2f + mPhotoView.getScrollX());
        ObjectAnimator yOa = ObjectAnimator.ofFloat(mPhotoView, "translationY", getTranslationY(mImageLocation[1]));
        
        AnimatorSet set = new AnimatorSet();
        set.setDuration(OPEN_AND_EXIT_ANIM_DURATION);
        set.playTogether(scaleOa, xOa, yOa);
        
        if (mIntAlpha > 0) {
            mRoot.setBackgroundColor(Color.TRANSPARENT);
        }
        if (mOnExitListener != null) {
            mOnExitListener.onStart();
        }
        set.start();
        
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mOnExitListener != null) {
                    mOnExitListener.onExit();
                }
            }
        }, getOpenAndExitAnimDuration());
        return true;
    }
    
    private long getOpenAndExitAnimDuration() {
        if (isOrientation()) {
            return OPEN_AND_EXIT_ANIM_DURATION_ORIENTATION;
        } else {
            return OPEN_AND_EXIT_ANIM_DURATION;
        }
    }
    
    /**
     * 获取ImageView实际绘制的图片大小,如果没有设置图片，则返回数据为0
     *
     * @return int[2], 下标0：实际绘制图片的宽度，下标1：实际绘制图片高度
     */
    private float[] getImageActualSize(ImageView imageView) {
        if (imageView == null || imageView.getDrawable() == null) {
            return new float[2];
        }
        
        //获得ImageView中Image的真实宽高，
        int dw = imageView.getDrawable().getBounds().width();
        int dh = imageView.getDrawable().getBounds().height();
        
        //获得ImageView中Image的变换矩阵
        Matrix m = imageView.getImageMatrix();
        float[] values = new float[9];
        m.getValues(values);
        
        //Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
        float sx = values[Matrix.MSCALE_X];
        float sy = values[Matrix.MSCALE_Y];
        
        //计算Image在屏幕上实际绘制的宽高
        float cw = dw * sx;
        float ch = dh * sy;
        return new float[]{cw, ch};
    }
    
    private float getTranslationY(int imageLocationY) {
        float translationY = imageLocationY
            - mPhotoView.getHeight() / 2f
            + mPhotoView.getScrollY();
        if (OSUtils.isVivo() || !mFullScreen) {
            translationY -= Utils.getStatusBarHeight(mContext);
        }
        return translationY;
    }
    
    /**
     * 获取{@link #mSrcImageContainer}中指定位置的view
     */
    @Nullable
    private View getSrcItemView(int position) {
        if (!(mSrcImageContainer instanceof ViewGroup)) {
            return mSrcImageContainer;
        }
        
        if (mSrcImageContainer instanceof AbsListView) {
            return ((AbsListView) mSrcImageContainer).getChildAt(position);
        }
        
        if (mSrcImageContainer instanceof RecyclerView) {
            RecyclerView.LayoutManager layoutManager = ((RecyclerView) mSrcImageContainer).getLayoutManager();
            if (layoutManager == null) {
                return mSrcImageContainer;
            }
            
            return layoutManager.findViewByPosition(position);
        }
        
        return null;
    }
    
    /**
     * 获取{@link #mSrcImageContainer}指定位置的图片的原始大小
     */
    private void getSrcViewSize(int position) {
        mImageSize[0] = 0;
        mImageSize[1] = 0;
        
        View itemView = getSrcItemView(position);
        if (itemView == null) {
            if (position != mDefaultShowPosition) {
                getSrcViewSize(mDefaultShowPosition);
            }
        }
        
        mImageSize[0] = itemView.getMeasuredWidth();
        mImageSize[1] = itemView.getMeasuredHeight();
    }
    
    /**
     * 获取{@link #mSrcImageContainer}指定位置的图片的原始中心点位置
     */
    private void getSrcViewLocation(int position) {
        mImageLocation[0] = 0;
        mImageLocation[1] = 0;
        
        View itemView = getSrcItemView(position);
        if (itemView == null) {
            if (position != mDefaultShowPosition) {
                getSrcViewLocation(mDefaultShowPosition);
            }
        }
        
        itemView.getLocationOnScreen(mImageLocation);
        if (isOrientation()) {
            // 横屏状态下，不管全屏还是非全屏、是否是异形屏，宽度坐标都似乎多出了一个状态栏高度
            // 因此在布局0,0位置放置一个基准View，实际View的x坐标减去基准View的x坐标则为实际坐标
            // 这样在横屏状态下打开和关闭都能做到无缝衔接
            mZeroLocationView.getLocationOnScreen(mImageLocationTemp);
            mImageLocation[0] -= mImageLocationTemp[0];
        }
        
        mImageLocation[0] += itemView.getMeasuredWidth() / 2;
        mImageLocation[1] += itemView.getMeasuredHeight() / 2;
    }
    
    /**
     * 当前显示界面是否横屏
     */
    private boolean isOrientation() {
        Context context = getContext();
        if (context == null) {
            return false;
        }
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return displayMetrics.widthPixels > displayMetrics.heightPixels;
        }
        
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x > point.y;
    }
    
    public void setData(@NonNull ImageLoader loadImage/*图片加载器*/,
                        Object url/*图片地址*/,
                        View srcImageContainer/*触发图片预览的View*/,
                        int defaultShowPosition/*如果预览多张图片,指定触发图片预览时默认展示图片位置*/,
                        int position/*当前预览图片的位置*/,
                        boolean needInAnim/*当前预览界面打开时是否需要动画*/,
                        long delayShowProgressTime/*图片加载框延迟展示时间*/,
                        Integer progressColor/*图片加载框颜色*/,
                        Drawable progressDrawable/*图片加载框图片*/,
                        boolean fullScreen/*是否全屏预览*/) {
        mLoadImage = loadImage;
        mUrl = url;
        mSrcImageContainer = srcImageContainer;
        mDefaultShowPosition = defaultShowPosition;
        mPosition = position;
        mNeedInAnim = needInAnim;
        mDelayShowProgressTime = delayShowProgressTime;
        mProgressColor = progressColor;
        mProgressDrawable = progressDrawable;
        mFullScreen = fullScreen;
    }
    
    public void setOnExitListener(OnExitListener onExitListener) {
        mOnExitListener = onExitListener;
    }
    
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        mOnLongClickListener = onLongClickListener;
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
    
}
