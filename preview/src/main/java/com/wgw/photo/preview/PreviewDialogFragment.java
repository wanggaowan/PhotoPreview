package com.wgw.photo.preview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnDismissListener;
import com.wgw.photo.preview.interfaces.OnLongClickListener;
import com.wgw.photo.preview.util.Utils;
import com.wgw.photo.preview.util.notch.CutOutMode;
import com.wgw.photo.preview.util.notch.NotchAdapterUtils;

import java.util.List;
import java.util.UUID;

/**
 * 预览界面根布局
 *
 * @author Created by 汪高皖 on 2019/3/20 0020 17:45
 */
public class PreviewDialogFragment extends DialogFragment {
    private String tag = UUID.randomUUID().toString();
    private Context mContext;
    
    private RelativeLayout mRootView;
    private ViewPager mViewPager;
    private LinearLayout mLlDotIndicator;
    private ImageView mIvSelectDot;
    private TextView mTvTextIndicator;
    
    private int mCurrentPagerIndex = 0;
    
    /**
     * 当前预览界面所依附的Activity
     */
    private AppCompatActivity mActivity;
    
    /**
     * 图片地址数据
     */
    private List<?> mPicUrls;
    
    /**
     * 源图片控件(小图)
     */
    private View mSrcImageContainer;
    
    /**
     * 图片加载器
     */
    private ImageLoader mImageLoader;
    
    /**
     * 图片长按点击监听
     */
    private OnLongClickListener mLongClickListener;
    
    /**
     * 预览关闭监听
     */
    private OnDismissListener mOnDismissListener;
    
    /**
     * 默认展示的图片位置
     */
    private int mDefaultShowPosition;
    
    /**
     * 数量指示器，默认为小圆点
     */
    private int mIndicatorType = IndicatorType.DOT;
    
    /**
     * 在调用{@link ImageLoader#onLoadImage(int, Object, ImageView)}时延迟展示loading框的时间，
     * < 0:不展示，=0:立即显示，>0:延迟给定时间显示，默认延迟100ms显示，如果在此时间内加载完成则不显示，否则显示
     */
    private long mDelayShowProgressTime = 100;
    
    /**
     * 图片loading时展示的loading框颜色,使用系统默认加载框图像，只是修改颜色
     */
    private Integer mProgressColor;
    
    /**
     * 自定义loading框Drawable，此参数作用于{@link ProgressBar#setIndeterminateDrawable(Drawable)}
     */
    private Drawable mProgressDrawable;
    
    public PreviewDialogFragment() {
        setCancelable(false);
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // 全屏处理
        boolean fullScreen = (mActivity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Window window = getDialog().getWindow();
        if (fullScreen) {
            NotchAdapterUtils.adapter(window, CutOutMode.SHORT_EDGES);
        }
        
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        super.onActivityCreated(savedInstanceState);
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(Color.BLACK);
            }
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0;
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            if (fullScreen) {
                lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            }
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }
    }
    
    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (RelativeLayout) inflater.inflate(R.layout.view_preview_root, null);
            mLlDotIndicator = mRootView.findViewById(R.id.ll_dot_indicator_photo_preview);
            mIvSelectDot = mRootView.findViewById(R.id.iv_select_dot_photo_preview);
            mTvTextIndicator = mRootView.findViewById(R.id.tv_text_indicator_photo_preview);
        }
        initViewData();
        return mRootView;
    }
    
    private void initViewData() {
        mLlDotIndicator.setVisibility(View.GONE);
        mIvSelectDot.setVisibility(View.GONE);
        mTvTextIndicator.setVisibility(View.GONE);
        prepareIndicator();
        prepareViewPager();
    }
    
    public void setActivity(@NonNull AppCompatActivity activity) {
        mActivity = activity;
    }
    
    public void setImageLoader(@NonNull ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }
    
    public void setLongClickListener(OnLongClickListener longClickListener) {
        mLongClickListener = longClickListener;
    }
    
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }
    
    public void setIndicatorType(@IndicatorType int indicatorType) {
        this.mIndicatorType = indicatorType;
    }
    
    public void setDelayShowProgressTime(long delayShowProgressTime) {
        mDelayShowProgressTime = delayShowProgressTime;
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
    }
    
    public void setProgressDrawable(Drawable progressDrawable) {
        mProgressDrawable = progressDrawable;
    }
    
    public void show(@NonNull View srcImageContainer, int defaultShowPosition, @NonNull List<?> picUrls) {
        mSrcImageContainer = srcImageContainer;
        mPicUrls = picUrls;
        mDefaultShowPosition = defaultShowPosition;
        mCurrentPagerIndex = defaultShowPosition;
        if (isAdded()) {
            initViewData();
        } else {
            show(mActivity.getSupportFragmentManager(), tag);
        }
        
    }
    
    /**
     * 准备用于展示预览图的ViePager数据
     */
    private void prepareViewPager() {
        if (mViewPager == null) {
            mViewPager = new NoTouchExceptionViewPager(mContext);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
            mViewPager.setLayoutParams(params);
            mViewPager.setId(mViewPager.hashCode());
            mRootView.addView(mViewPager, 0);
        }
        
        PhotoPreviewPagerAdapter adapter;
        if (mViewPager.getAdapter() == null) {
            adapter = new PhotoPreviewPagerAdapter(getChildFragmentManager(), mPicUrls.size());
            adapter.setFragmentOnExitListener(new PhotoPreviewFragment.OnExitListener() {
                @Override
                public void onStart() {
                    mLlDotIndicator.setVisibility(View.GONE);
                    mIvSelectDot.setVisibility(View.GONE);
                    mTvTextIndicator.setVisibility(View.GONE);
                }
                
                @Override
                public void onExit() {
                    dismissAllowingStateLoss();
                    mRootView.removeView(mViewPager);
                    mViewPager = null;
                    ViewParent parent = mRootView.getParent();
                    if (parent instanceof ViewGroup) {
                        ((ViewGroup) parent).removeView(mRootView);
                    }
                    
                    if (mOnDismissListener != null) {
                        mOnDismissListener.onDismiss();
                    }
                }
            });
            
            mViewPager.setAdapter(adapter);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (mLlDotIndicator.getVisibility() == View.VISIBLE) {
                        float dx = mLlDotIndicator.getChildAt(1).getX() - mLlDotIndicator.getChildAt(0).getX();
                        mIvSelectDot.setTranslationX((position * dx) + positionOffset * dx);
                    }
                }
                
                @SuppressLint("SetTextI18n")
                @Override
                public void onPageSelected(int position) {
                    mCurrentPagerIndex = position;
                    // 设置文字版本当前页的值
                    if (mTvTextIndicator.getVisibility() == View.VISIBLE) {
                        mTvTextIndicator.setText((mCurrentPagerIndex + 1) + " / " + mPicUrls.size());
                    }
                }
                
                @Override
                public void onPageScrollStateChanged(int position) {
                
                }
            });
        } else {
            adapter = ((PhotoPreviewPagerAdapter) mViewPager.getAdapter());
            adapter.setData(mPicUrls.size());
        }
        
        adapter.setOnUpdateFragmentDataListener(new PhotoPreviewPagerAdapter.OnUpdateFragmentDataListener() {
            @Override
            public void onUpdate(PhotoPreviewFragment fragment, int position) {
                boolean fullScreen = (mActivity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
                fragment.setData(mImageLoader, position, mPicUrls.get(position), getViewSize(position), getViewLocation(position),
                    position == mDefaultShowPosition, mDelayShowProgressTime, mProgressColor, mProgressDrawable, fullScreen);
                fragment.setOnLongClickListener(mLongClickListener);
            }
        });
        
        mViewPager.setCurrentItem(mCurrentPagerIndex);
    }
    
    
    /**
     * 准备滑动指示器数据
     */
    @SuppressLint("SetTextI18n")
    private void prepareIndicator() {
        if (mPicUrls.size() >= 2 && mPicUrls.size() <= 9 && IndicatorType.DOT == mIndicatorType) {
            mLlDotIndicator.removeAllViews();
            final LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            
            // 未选中小圆点的间距
            dotParams.rightMargin = Utils.dp2px(mContext, 12);
            
            // 创建未选中的小圆点
            for (int i = 0; i < mPicUrls.size(); i++) {
                ImageView iv = new ImageView(mContext);
                iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_selected_dot));
                iv.setLayoutParams(dotParams);
                mLlDotIndicator.addView(iv);
            }
            
            mLlDotIndicator.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mIvSelectDot.getLayoutParams();
                    // 设置选中小圆点的左边距
                    params.leftMargin = (int) mLlDotIndicator.getChildAt(0).getX();
                    mIvSelectDot.setTranslationX((dotParams.rightMargin * mCurrentPagerIndex + mLlDotIndicator.
                        getChildAt(0).getWidth() * mCurrentPagerIndex));
                    mIvSelectDot.setVisibility(View.VISIBLE);
                }
            });
            
            mLlDotIndicator.setVisibility(View.VISIBLE);
        } else if (mPicUrls.size() > 9) {
            mTvTextIndicator.setVisibility(View.VISIBLE);
            mTvTextIndicator.setText((mCurrentPagerIndex + 1) + "/" + mPicUrls.size());
        }
    }
    
    /**
     * 获取itemView
     */
    @Nullable
    private View getItemView(int position) {
        if (!(mSrcImageContainer instanceof ViewGroup)) {
            return mSrcImageContainer;
        }
        
        if (mSrcImageContainer instanceof AbsListView) {
            return ((AbsListView) mSrcImageContainer).getChildAt(position);
        }
        
        if (mSrcImageContainer instanceof RecyclerView) {
            RecyclerView.LayoutManager layoutManager = ((RecyclerView) mSrcImageContainer).getLayoutManager();
            if (layoutManager == null) {
                return null;
            }
            return layoutManager.findViewByPosition(position);
        }
        
        return null;
    }
    
    /**
     * 获取指定位置的图片的原始大小
     */
    private int[] getViewSize(int position) {
        int[] result = new int[2];
        View itemView = getItemView(position);
        if (itemView == null) {
            if (position != mDefaultShowPosition) {
                return getViewSize(mDefaultShowPosition);
            }
            return result;
        }
        result[0] = itemView.getMeasuredWidth();
        result[1] = itemView.getMeasuredHeight();
        return result;
    }
    
    /**
     * 获取指定位置的图片的原始中心点位置
     */
    private int[] getViewLocation(int position) {
        int[] result = new int[2];
        View itemView = getItemView(position);
        if (itemView == null) {
            if (position != mDefaultShowPosition) {
                return getViewLocation(mDefaultShowPosition);
            }
            return result;
        }
        itemView.getLocationOnScreen(result);
        result[0] += itemView.getMeasuredWidth() / 2;
        result[1] += itemView.getMeasuredHeight() / 2;
        return result;
    }
}
