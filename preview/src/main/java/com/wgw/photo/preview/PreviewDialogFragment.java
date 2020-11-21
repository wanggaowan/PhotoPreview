package com.wgw.photo.preview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wgw.photo.preview.interfaces.IFindThumbnailView;
import com.wgw.photo.preview.interfaces.OnDismissListener;
import com.wgw.photo.preview.util.SpannableString;
import com.wgw.photo.preview.util.Utils;
import com.wgw.photo.preview.util.notch.CutOutMode;
import com.wgw.photo.preview.util.notch.NotchAdapterUtils;

import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle.State;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * 预览界面根布局
 *
 * @author Created by wanggaowan on 2019/3/20 0020 17:45
 */
public class PreviewDialogFragment extends DialogFragment {
    
    private static final int VIEW_PAGER_ID = 13679;
    
    private static final int VIEW_PAGER_ID_NEXT = 23679;
    
    private final String tag = UUID.randomUUID().toString();
    private Context mContext;
    
    private RelativeLayout mRootView;
    private ViewPager mViewPager;
    private LinearLayout mLlDotIndicator;
    private ImageView mIvSelectDot;
    private TextView mTvTextIndicator;
    
    private FragmentActivity mActivity;
    private Config mConfig;
    private View mThumbnailView;
    private IFindThumbnailView mFindThumbnailView;
    private int mCurrentPagerIndex = 0;
    
    /**
     * 是否添加到Activity
     */
    private boolean mAdd;
    
    /**
     * 是否已经Dismiss
     */
    private boolean mDismiss;
    
    /**
     * 界面关闭时是否需要调用{@link OnDismissListener}
     */
    private boolean mCallOnDismissListener = true;
    
    /**
     * 是否在当前界面OnDismiss调用{@link OnDismissListener}
     */
    private boolean mCallOnDismissListenerInThisOnDismiss;
    
    public PreviewDialogFragment() {
        setCancelable(false);
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // 说明是被回收后恢复，此时不恢复
            super.onActivityCreated(savedInstanceState);
            return;
        }
        
        Window window = getDialog().getWindow();
        if (window == null) {
            super.onActivityCreated(null);
            return;
        }
        
        // 全屏处理
        window.requestFeature(Window.FEATURE_NO_TITLE);
        boolean fullScreen = isFullScreen();
        NotchAdapterUtils.adapter(window, CutOutMode.SHORT_EDGES);
        super.onActivityCreated(null);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 需要设置这个才能设置状态栏和导航栏颜色，此时布局内容可绘制到状态栏之下
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0;
        lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        if (fullScreen) {
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            lp.flags |= LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
        }
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }
    
    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (RelativeLayout) inflater.inflate(R.layout.view_preview_root, null);
            mViewPager = mRootView.findViewById(R.id.viewpager);
            mLlDotIndicator = mRootView.findViewById(R.id.ll_dot_indicator_photo_preview);
            mIvSelectDot = mRootView.findViewById(R.id.iv_select_dot_photo_preview);
            mTvTextIndicator = mRootView.findViewById(R.id.tv_text_indicator_photo_preview);
        }
        
        if (savedInstanceState == null) {
            initViewData();
            mDismiss = false;
        } else {
            // 被回收后恢复，则关闭弹窗
            dismissAllowingStateLoss();
        }
        
        return mRootView;
    }
    
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mAdd = false;
        mDismiss = true;
        if (mConfig != null && mConfig.onDismissListener != null
            && mCallOnDismissListenerInThisOnDismiss
            && mCallOnDismissListener) {
            mConfig.onDismissListener.onDismiss();
        }
        
        if (mRootView != null) {
            ViewParent parent = mRootView.getParent();
            if (parent instanceof ViewGroup) {
                // 为了下次重用mRootView
                ((ViewGroup) parent).removeView(mRootView);
            }
        }
    }
    
    /**
     * 是否全屏显示
     */
    private boolean isFullScreen() {
        if (mConfig.fullScreen != null) {
            return mConfig.fullScreen;
        }
        
        // 跟随打开预览界面的显示状态
        return (mActivity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
    }
    
    public void show(FragmentActivity activity, Config config, View thumbnailView) {
        mActivity = activity;
        mConfig = config;
        mThumbnailView = thumbnailView;
        FragmentManager manager = mActivity.getSupportFragmentManager();
        // isAdded()并不一定靠谱，可能存在一定的延时性，因此通过查找manager是否存在当前对象来做进一步判断
        if (isAdded() || manager.findFragmentByTag(tag) != null || mAdd) {
            initViewData();
        } else {
            mAdd = true;
            show(manager, tag);
        }
    }
    
    public void show(FragmentActivity activity, Config config, @NonNull IFindThumbnailView findThumbnailView) {
        mActivity = activity;
        mConfig = config;
        mFindThumbnailView = findThumbnailView;
        FragmentManager manager = mActivity.getSupportFragmentManager();
        // isAdded()并不一定靠谱，可能存在一定的延时性，因此通过查找manager是否存在当前对象来做进一步判断
        if (isAdded() || manager.findFragmentByTag(tag) != null || mAdd) {
            initViewData();
        } else {
            mAdd = true;
            show(manager, tag);
        }
    }
    
    /**
     * 退出预览
     *
     * @param callBack 是否需要执行{@link OnDismissListener}回调
     */
    public void dismiss(boolean callBack) {
        if (mDismiss || !getLifecycle().getCurrentState().isAtLeast(State.CREATED)) {
            return;
        }
        
        mCallOnDismissListener = callBack;
        if (mViewPager == null) {
            
            mCallOnDismissListenerInThisOnDismiss = true;
            dismissAllowingStateLoss();
        } else {
            PagerAdapter adapter = mViewPager.getAdapter();
            if (!(adapter instanceof PhotoPreviewPagerAdapter)) {
                mCallOnDismissListenerInThisOnDismiss = true;
                dismissAllowingStateLoss();
                return;
            }
            
            int position = mViewPager.getCurrentItem();
            Fragment fragment = ((PhotoPreviewPagerAdapter) adapter).findFragment(mViewPager, position);
            if (!(fragment instanceof PhotoPreviewFragment)) {
                mCallOnDismissListenerInThisOnDismiss = true;
                dismissAllowingStateLoss();
                return;
            }
            
            boolean exit = ((PhotoPreviewFragment) fragment).exit();
            if (!exit) {
                mCallOnDismissListenerInThisOnDismiss = true;
                dismissAllowingStateLoss();
            }
        }
    }
    
    private void initViewData() {
        mCurrentPagerIndex = mConfig.defaultShowPosition;
        mLlDotIndicator.setVisibility(View.GONE);
        mIvSelectDot.setVisibility(View.GONE);
        mTvTextIndicator.setVisibility(View.GONE);
        prepareIndicator();
        prepareViewPager();
    }
    
    /**
     * 准备用于展示预览图的ViePager数据
     */
    private void prepareViewPager() {
        // 每次预览的时候，如果不动态修改每个ViewPager的Id
        // 那么预览多张图片时，如果第一次点击位置1预览然后关闭，再点击位置2，预览图片打开的还是位置1预览图
        if (mViewPager.getId() == VIEW_PAGER_ID) {
            mViewPager.setId(VIEW_PAGER_ID_NEXT);
        } else {
            mViewPager.setId(VIEW_PAGER_ID);
        }
        
        List<Object> sources = mConfig.sources;
        PhotoPreviewPagerAdapter adapter = new PhotoPreviewPagerAdapter(getChildFragmentManager(), sources);
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
                if (mConfig.onDismissListener != null && mCallOnDismissListener) {
                    mConfig.onDismissListener.onDismiss();
                }
            }
        });
        
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mLlDotIndicator.getVisibility() == View.VISIBLE) {
                    float dx = mLlDotIndicator.getChildAt(1).getX() - mLlDotIndicator.getChildAt(0).getX();
                    mIvSelectDot.setTranslationX((position * dx) + positionOffset * dx);
                }
            }
            
            @Override
            public void onPageSelected(int position) {
                mCurrentPagerIndex = position;
                // 设置文字版本当前页的值
                if (mTvTextIndicator.getVisibility() == View.VISIBLE) {
                    updateTextIndicator();
                }
            }
            
            @Override
            public void onPageScrollStateChanged(int position) {
            
            }
        });
        
        adapter.setOnUpdateFragmentDataListener(new PhotoPreviewPagerAdapter.OnUpdateFragmentDataListener() {
            @Override
            public void onUpdate(PhotoPreviewFragment fragment, int position) {
                fragment.setData(mConfig,
                    mThumbnailView,
                    mFindThumbnailView,
                    position);
            }
        });
        
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mCurrentPagerIndex);
    }
    
    /**
     * 准备滑动指示器数据
     */
    private void prepareIndicator() {
        int sourceSize = mConfig.sources == null ? 0 : mConfig.sources.size();
        if (sourceSize >= 2 && sourceSize <= 9 && IndicatorType.DOT == mConfig.indicatorType) {
            mLlDotIndicator.removeAllViews();
            
            if (mConfig.selectIndicatorColor != 0xFFFFFFFF) {
                Drawable drawable = mIvSelectDot.getDrawable();
                GradientDrawable gradientDrawable;
                if (drawable instanceof GradientDrawable) {
                    gradientDrawable = (GradientDrawable) drawable;
                } else {
                    gradientDrawable = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.selected_dot);
                }
                
                gradientDrawable.setColorFilter(mConfig.selectIndicatorColor, Mode.SRC_OVER);
                mIvSelectDot.setImageDrawable(gradientDrawable);
            }
            
            final LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            
            // 未选中小圆点的间距
            dotParams.rightMargin = Utils.dp2px(mContext, 12);
            
            // 创建未选中的小圆点
            for (int i = 0; i < sourceSize; i++) {
                AppCompatImageView iv = new AppCompatImageView(mContext);
                GradientDrawable shapeDrawable = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.no_selected_dot);
                if (mConfig.normalIndicatorColor != 0xFFAAAAAA) {
                    shapeDrawable.setColorFilter(mConfig.normalIndicatorColor, Mode.SRC_OVER);
                }
                iv.setImageDrawable(shapeDrawable);
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
        } else if (sourceSize > 1) {
            mTvTextIndicator.setVisibility(View.VISIBLE);
            updateTextIndicator();
        }
    }
    
    private void updateTextIndicator() {
        int sourceSize = mConfig.sources == null ? 0 : mConfig.sources.size();
        SpannableString.Builder.appendMode()
            .addSpan(String.valueOf(mCurrentPagerIndex + 1))
            .color(mConfig.selectIndicatorColor)
            .addSpan(" / " + sourceSize)
            .color(mConfig.normalIndicatorColor)
            .apply(mTvTextIndicator);
    }
}
