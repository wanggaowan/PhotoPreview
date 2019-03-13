package com.wgw.photo.preview;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnDismissListener;
import com.wgw.photo.preview.interfaces.OnLongClickListener;

import java.util.Arrays;
import java.util.List;

/**
 * 图片预览，支持预览单张，多张图片
 *
 * @author Created by 汪高皖 on 2019/2/26 0026 16:55
 */
public class PhotoPreview {
    private static int VIEW_PAGER_ID = -10000;
    
    private RelativeLayout mRootView;
    private ViewPager mViewPager;
    private LinearLayout mLlDotIndicator;
    private ImageView mIvSelectDot;
    private TextView mTvTextIndicator;
    
    private int mCurrentPagerIndex = 0;
    
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
    private ImageLoader mLoadImage;
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
    
    /**
     * @param activity  当前图片预览所处Activity
     * @param loadImage 图片加载回调，图片加载逻辑需要外部自己实现
     */
    public PhotoPreview(@NonNull AppCompatActivity activity, @NonNull ImageLoader loadImage) {
        mActivity = activity;
        mLoadImage = loadImage;
    }
    
    public void setLoadImage(@NonNull ImageLoader loadImage) {
        mLoadImage = loadImage;
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
    
    /**
     * @param srcImageContainer 源视图，可以是{@link AbsListView}、{@link RecyclerView}或{@link View}，
     *                          如果是{@link AbsListView}或{@link RecyclerView}，请确保children数量 >= picUrls size
     * @param picUrls           图片Url数据，该数据决定可预览图片的数量
     */
    @SuppressLint({"SetTextI18n", "InflateParams"})
    public void show(View srcImageContainer, @NonNull Object... picUrls) {
        show(srcImageContainer, Arrays.asList(picUrls));
    }
    
    /**
     * @param srcImageContainer 源视图，可以是{@link AbsListView}、{@link RecyclerView}或{@link View}，
     *                          如果是{@link AbsListView}或{@link RecyclerView}，请确保children数量 >= picUrls size
     * @param picUrls           图片Url数据，该数据决定可预览图片的数量
     */
    @SuppressLint({"SetTextI18n", "InflateParams"})
    public void show(View srcImageContainer, @NonNull List<?> picUrls) {
        show(srcImageContainer, 0, picUrls);
    }
    
    /**
     * @param srcImageContainer 源视图，可以是{@link AbsListView}、{@link RecyclerView}或{@link View}，
     *                          如果是{@link AbsListView}或{@link RecyclerView}，请确保children数量 >= picUrls size
     * @param picUrls           图片Url数据，该数据决定可预览图片的数量
     */
    @SuppressLint({"SetTextI18n", "InflateParams"})
    public void show(@NonNull View srcImageContainer, int defaultShowPosition, @NonNull Object... picUrls) {
        show(srcImageContainer, defaultShowPosition, Arrays.asList(picUrls));
    }
    
    /**
     * @param srcImageContainer   源视图，可以是{@link AbsListView}、{@link RecyclerView}或{@link View}，
     *                            如果是{@link AbsListView}或{@link RecyclerView}，请确保children数量 >= picUrls size
     * @param picUrls             图片Url数据，该数据决定可预览图片的数量
     * @param defaultShowPosition 默认展示图片的位置
     */
    @SuppressLint({"SetTextI18n", "InflateParams"})
    public void show(@NonNull View srcImageContainer, int defaultShowPosition, @NonNull List<?> picUrls) {
        mSrcImageContainer = srcImageContainer;
        mPicUrls = picUrls;
        mDefaultShowPosition = defaultShowPosition;
        mCurrentPagerIndex = defaultShowPosition;
        final ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
    
        mRootView = mActivity.findViewById(R.id.rl_root_photo_preview);
        if (mRootView == null) {
            View root = LayoutInflater.from(mActivity).inflate(R.layout.view_preview_root, null);
            mRootView = root.findViewById(R.id.rl_root_photo_preview);
            decorView.addView(mRootView);
        }
        mRootView.setVisibility(View.VISIBLE);
    
        if (mLlDotIndicator == null) {
            mLlDotIndicator = mRootView.findViewById(R.id.ll_dot_indicator_photo_preview);
            mIvSelectDot = mRootView.findViewById(R.id.iv_select_dot_photo_preview);
            mTvTextIndicator = mRootView.findViewById(R.id.tv_text_indicator_photo_preview);
        }
        
        mLlDotIndicator.setVisibility(View.GONE);
        mIvSelectDot.setVisibility(View.GONE);
        mTvTextIndicator.setVisibility(View.GONE);
        prepareIndicator(mActivity);
        prepareViewPager(mActivity);
    }
    
    /**
     * 准备用于展示预览图的ViePager数据
     */
    private void prepareViewPager(final AppCompatActivity activity) {
        if (mViewPager == null) {
            mViewPager = new NoTouchExceptionViewPager(mActivity);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
            mViewPager.setLayoutParams(params);
            mViewPager.setId(mViewPager.hashCode());
        }
        mRootView.addView(mViewPager, 0);
        
        PhotoPreviewPagerAdapter adapter;
        if (mViewPager.getAdapter() == null) {
            adapter = new PhotoPreviewPagerAdapter(activity.getSupportFragmentManager(), mPicUrls.size());
            adapter.setFragmentOnExitListener(new PhotoPreviewFragment.OnExitListener() {
                @Override
                public void onExit() {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRootView.removeView(mViewPager);
                            mRootView.setVisibility(View.GONE);
                            if (mOnDismissListener != null) {
                                mOnDismissListener.onDismiss();
                            }
                        }
                    });
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
                fragment.setData(mLoadImage, position, mPicUrls.get(position), getViewSize(position), getViewLocation(position),
                    position == mDefaultShowPosition, mDelayShowProgressTime, mProgressColor, mProgressDrawable);
                fragment.setOnLongClickListener(mLongClickListener);
            }
        });
        
        mViewPager.setCurrentItem(mCurrentPagerIndex);
    }
    
    /**
     * 准备滑动指示器数据
     */
    @SuppressLint("SetTextI18n")
    private void prepareIndicator(AppCompatActivity activity) {
        if (mPicUrls.size() >= 2 && mPicUrls.size() <= 9 && IndicatorType.DOT == mIndicatorType) {
            mLlDotIndicator.removeAllViews();
            final LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            
            // 未选中小圆点的间距
            dotParams.rightMargin = Utils.dp2px(activity, 12);
            
            // 创建未选中的小圆点
            for (int i = 0; i < mPicUrls.size(); i++) {
                ImageView iv = new ImageView(activity);
                iv.setImageDrawable(activity.getResources().getDrawable(R.drawable.no_selected_dot));
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
        itemView.getLocationInWindow(result);
        result[0] += itemView.getMeasuredWidth() / 2;
        result[1] += itemView.getMeasuredHeight() / 2;
        return result;
    }
}
