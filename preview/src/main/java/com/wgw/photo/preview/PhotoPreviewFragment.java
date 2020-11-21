package com.wgw.photo.preview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.wgw.photo.preview.interfaces.IFindThumbnailView;
import com.wgw.photo.preview.util.MatrixUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle.State;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 图片预览Fragment
 *
 * @author Created by wanggaowan on 2019/2/27 0027 11:24
 */
public class PhotoPreviewFragment extends Fragment {
    private static final long OPEN_AND_EXIT_ANIM_DURATION = 200;
    
    private Context mContext;
    FrameLayout mRoot;
    private PhotoView mPhotoView;
    private ProgressBar mLoading;
    
    private ScheduledExecutorService mService;
    private ScheduledFuture<?> mSchedule;
    private Handler mHandler;
    
    private Config mConfig;
    private View mThumbnailView;
    private IFindThumbnailView mFindThumbnailView;
    private int mPosition;
    private boolean mNeedInAnim;
    
    private final int[] mSrcImageSize = new int[2];
    private final int[] mImageLocationTemp = new int[2];
    private final int[] mSrcImageLocation = new int[2];
    
    private OnExitListener mOnExitListener;
    
    @Override
    public void onAttach(@NonNull Context context) {
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
            mRoot.setFocusableInTouchMode(true);
            mRoot.requestFocus();
            
            mPhotoView = view.findViewById(R.id.photoView);
            mPhotoView.setPhotoPreviewFragment(this);
            mLoading = view.findViewById(R.id.loading);
            
            initEvent();
            onLoadData();
        }  // savedInstanceState != null的时候，预览窗口（父对象PreviewDialogFragment）将关闭，因此不做任何其它处理
        
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
        
        mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoView.getAttacher().getScale() > 1) {
                    return;
                }
                exit();
            }
        });
        
        mPhotoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mConfig.onLongClickListener != null) {
                    mConfig.onLongClickListener.onLongClick(mRoot);
                }
                return true;
            }
        });
        
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoView.getAttacher().getScale() > 1) {
                    return;
                }
                exit();
            }
        });
    }
    
    private void onLoadData() {
        mPhotoView.setVisibility(View.INVISIBLE);
        mPhotoView.setImageDrawable(null);
        if (mConfig.imageLoader != null) {
            if (mConfig.sources != null && mPosition < mConfig.sources.size() && mPosition >= 0) {
                mConfig.imageLoader.onLoadImage(mPosition, mConfig.sources.get(mPosition), mPhotoView);
            } else {
                mConfig.imageLoader.onLoadImage(mPosition, null, mPhotoView);
            }
        }
        
        checkLoadResult();
        mRoot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.black));
        if (mNeedInAnim) {
            mNeedInAnim = false;
            // 添加点击进入时的动画
            mPhotoView.post(new Runnable() {
                @Override
                public void run() {
                    mPhotoView.setVisibility(View.VISIBLE);
                    boolean doSuccess = getSrcViewSize(mPosition);
                    if (!doSuccess) {
                        ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", 0, 1f);
                        scaleOa.setDuration(getOpenAndExitAnimDuration());
                        scaleOa.start();
                        return;
                    }
                    
                    float scaleX = mPhotoView.getWidth() <= 0 ? 0 : mSrcImageSize[0] * 1f / mPhotoView.getWidth();
                    float scaleY = mPhotoView.getHeight() <= 0 ? 0 : mSrcImageSize[1] * 1f / mPhotoView.getHeight();
                    float scale = 0;
                    if (scaleX > 0 && scaleY > 0) {
                        Matrix m = new Matrix();
                        m.postScale(scaleX, scaleY);
                        scale = MatrixUtils.getScale(m);
                    }
                    
                    getSrcViewLocation(mPosition);
                    float fromX = mSrcImageLocation[0] - mPhotoView.getWidth() / 2f;
                    float toX = 0;
                    float fromY = mSrcImageLocation[1] - mPhotoView.getHeight() / 2f + mPhotoView.getScrollY();
                    float toY = 0;
                    
                    ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", scale, 1f);
                    ObjectAnimator xOa = ObjectAnimator.ofFloat(mPhotoView, "translationX", fromX, toX);
                    ObjectAnimator yOa = ObjectAnimator.ofFloat(mPhotoView, "translationY", fromY, toY);
                    
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
        if (mConfig.delayShowProgressTime < 0) {
            mLoading.setVisibility(View.GONE);
            return;
        }
        
        if (mConfig.progressDrawable != null) {
            mLoading.setIndeterminateDrawable(mConfig.progressDrawable);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mConfig.progressColor != null) {
            mLoading.setIndeterminateTintList(ColorStateList.valueOf(mConfig.progressColor));
        }
        
        mLoading.setVisibility(mConfig.delayShowProgressTime == 0 ? View.VISIBLE : View.GONE);
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
        }, mConfig.delayShowProgressTime == 0 ? 100 : mConfig.delayShowProgressTime, 100, TimeUnit.MILLISECONDS);
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
        
        long duration = getOpenAndExitAnimDuration();
        boolean doSuccess = getSrcViewLocation(mPosition);
        if (!doSuccess) {
            ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", 1f, 0f);
            scaleOa.setDuration(getOpenAndExitAnimDuration());
            
            if (mPhotoView.getAlpha() > 0) {
                mRoot.setBackgroundColor(Color.TRANSPARENT);
            }
            if (mOnExitListener != null) {
                mOnExitListener.onStart();
            }
            scaleOa.start();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mOnExitListener != null) {
                        mOnExitListener.onExit();
                    }
                }
            }, duration);
            return true;
        }
        
        float[] imageAcSize = getImageActualSize(mPhotoView);
        float scale = 0;
        if (imageAcSize[0] > 0 && imageAcSize[1] > 0) {
            Matrix m = new Matrix();
            getSrcViewSize(mPosition);
            m.postScale((mSrcImageSize[0] * 1f / imageAcSize[0]), (mSrcImageSize[1] * 1f / imageAcSize[1]));
            scale = MatrixUtils.getScale(m);
        }
        
        float toX = mSrcImageLocation[0] - mPhotoView.getWidth() / 2f + mPhotoView.getScrollX();
        float toY = mSrcImageLocation[1] - mPhotoView.getHeight() / 2f + mPhotoView.getScrollY();
        
        ObjectAnimator scaleOa = ObjectAnimator.ofFloat(mPhotoView, "scale", scale * mPhotoView.getScale());
        ObjectAnimator xOa = ObjectAnimator.ofFloat(mPhotoView, "translationX", toX);
        ObjectAnimator yOa = ObjectAnimator.ofFloat(mPhotoView, "translationY", toY);
        
        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);
        set.playTogether(scaleOa, xOa, yOa);
        
        if (mPhotoView.getAlpha() > 0) {
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
        }, duration);
        return true;
    }
    
    /**
     * 获取动画时间，计划以后动态计算
     */
    private long getOpenAndExitAnimDuration() {
        return OPEN_AND_EXIT_ANIM_DURATION;
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
        
        // 获得ImageView中Image的真实宽高，
        int dw = imageView.getDrawable().getBounds().width();
        int dh = imageView.getDrawable().getBounds().height();
        
        // 获得ImageView中Image的变换矩阵
        Matrix m = imageView.getImageMatrix();
        float[] values = new float[9];
        m.getValues(values);
        
        // Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
        float sx = values[Matrix.MSCALE_X];
        float sy = values[Matrix.MSCALE_Y];
        
        // 计算Image在屏幕上实际绘制的宽高
        float cw = dw * sx;
        float ch = dh * sy;
        return new float[]{cw, ch};
    }
    
    /**
     * 获取{@link #mThumbnailView}、{@link #mFindThumbnailView}中指定位置的缩略图
     */
    @Nullable
    private View getThumbnailItemView(int position) {
        if (mThumbnailView != null) {
            if (!(mThumbnailView instanceof ViewGroup)) {
                return mThumbnailView;
            }
            
            if (mThumbnailView instanceof AbsListView) {
                return ((AbsListView) mThumbnailView).getChildAt(position);
            }
            
            if (mThumbnailView instanceof RecyclerView) {
                RecyclerView.LayoutManager layoutManager = ((RecyclerView) mThumbnailView).getLayoutManager();
                if (layoutManager == null) {
                    return mThumbnailView;
                }
                
                return layoutManager.findViewByPosition(position);
            }
        } else if (mFindThumbnailView != null) {
            return mFindThumbnailView.findView(position);
        }
        
        return null;
    }
    
    /**
     * 获取{@link #mThumbnailView}、{@link #mFindThumbnailView}指定位置的略图的大小
     */
    private boolean getSrcViewSize(int position) {
        mSrcImageSize[0] = 0;
        mSrcImageSize[1] = 0;
        
        View itemView = getThumbnailItemView(position);
        if (itemView == null) {
            if (position != mConfig.defaultShowPosition) {
                return getSrcViewSize(mConfig.defaultShowPosition);
            }
            return false;
        }
        
        mSrcImageSize[0] = itemView.getMeasuredWidth();
        mSrcImageSize[1] = itemView.getMeasuredHeight();
        return true;
    }
    
    /**
     * 获取{@link #mThumbnailView}、{@link #mFindThumbnailView}指定位置缩略图的位置
     */
    private boolean getSrcViewLocation(int position) {
        mSrcImageLocation[0] = 0;
        mSrcImageLocation[1] = 0;
        
        View itemView = getThumbnailItemView(position);
        if (itemView == null) {
            if (position != mConfig.defaultShowPosition) {
                return getSrcViewLocation(mConfig.defaultShowPosition);
            }
            return false;
        }
        
        itemView.getLocationOnScreen(mSrcImageLocation);
        FragmentActivity activity = getActivity();
        if (activity != null && activity.getWindow() != null) {
            // 计算activity decorView的坐标，以此作为基准，计算缩略图在屏幕中的位置
            // 这样做的目的，一个是处理状态栏是否显示问题，如果显示状态栏，则getLocationOnScreen计算的高度要去除状态栏高度
            // 二是处理异形屏，在横屏状态下，异形屏耳朵区域可能黑屏显示，因此X轴坐标要去除黑屏区域宽度
            View view = activity.getWindow().getDecorView();
            view.getLocationOnScreen(mImageLocationTemp);
            mSrcImageLocation[0] -= mImageLocationTemp[0];
            mSrcImageLocation[1] -= mImageLocationTemp[1];
        }
        
        mSrcImageLocation[0] += itemView.getMeasuredWidth() / 2;
        mSrcImageLocation[1] += itemView.getMeasuredHeight() / 2;
        return true;
    }
    
    public void setData(@NonNull Config config,
                        View thumbnailView/*触发图片预览的View*/,
                        IFindThumbnailView findThumbnailView/*查找指定位置缩略图*/,
                        int position/*当前预览图片的位置*/) {
        mConfig = config;
        mThumbnailView = thumbnailView;
        mFindThumbnailView = findThumbnailView;
        mPosition = position;
        mNeedInAnim = position == config.defaultShowPosition;
    }
    
    public void setOnExitListener(OnExitListener onExitListener) {
        mOnExitListener = onExitListener;
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
