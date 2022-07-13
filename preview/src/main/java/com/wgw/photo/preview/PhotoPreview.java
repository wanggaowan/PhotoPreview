package com.wgw.photo.preview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.wgw.photo.preview.interfaces.IFindThumbnailView;
import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnDismissListener;
import com.wgw.photo.preview.interfaces.OnLongClickListener;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

/**
 * 图片预览，支持预览单张，多张图片。
 * 每个Activity持有同一个预览对象，因此{@link PhotoPreview#PhotoPreview(FragmentActivity)}、
 * {@link PhotoPreview#with(FragmentActivity)}对于同一个activity操作的是同一个对象
 *
 * @author Created by wanggaowan on 2019/2/26 0026 16:55
 */
public class PhotoPreview {
    
    /**
     * 全局图片加载器
     */
    private static ImageLoader globalImageLoader = null;
    
    /**
     * 图片预览池，一个Activity持有一个预览对象
     */
    private static final Map<String, WeakReference<PreviewDialogFragment>> DIALOG_POOL = new HashMap<>();
    
    private final FragmentActivity mFragmentActivity;
    private final Fragment mFragment;
    private final Config mConfig;
    
    /**
     * 设置图片全局加载器
     */
    public static void setGlobalImageLoader(ImageLoader imageLoader) {
        globalImageLoader = imageLoader;
    }
    
    private static PreviewDialogFragment getDialog(final FragmentActivity activity, boolean noneCreate) {
        Fragment fragmentByTag = activity.getSupportFragmentManager().findFragmentByTag(PreviewDialogFragment.FRAGMENT_TAG);
        if (fragmentByTag instanceof PreviewDialogFragment) {
            return (PreviewDialogFragment) fragmentByTag;
        }
        
        final String name = activity.toString();
        WeakReference<PreviewDialogFragment> reference = DIALOG_POOL.get(name);
        PreviewDialogFragment fragment = reference == null ? null : reference.get();
        if (fragment == null) {
            if (noneCreate) {
                fragment = new PreviewDialogFragment();
                reference = new WeakReference<>(fragment);
                DIALOG_POOL.put(name, reference);
                activity.getLifecycle().addObserver(new LifecycleObserver() {
                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    public void onDestroy() {
                        activity.getLifecycle().removeObserver(this);
                        DIALOG_POOL.remove(name);
                    }
                });
            } else {
                DIALOG_POOL.remove(name);
            }
        }
        
        return fragment;
    }
    
    private static PreviewDialogFragment getDialog(final Fragment parentFragment, boolean noneCreate) {
        Fragment fragmentByTag = parentFragment.getChildFragmentManager().findFragmentByTag(PreviewDialogFragment.FRAGMENT_TAG);
        if (fragmentByTag instanceof PreviewDialogFragment) {
            return (PreviewDialogFragment) fragmentByTag;
        }
        
        final String name = parentFragment.toString();
        WeakReference<PreviewDialogFragment> reference = DIALOG_POOL.get(name);
        PreviewDialogFragment fragment = reference == null ? null : reference.get();
        if (fragment == null) {
            if (noneCreate) {
                fragment = new PreviewDialogFragment();
                reference = new WeakReference<>(fragment);
                DIALOG_POOL.put(name, reference);
                parentFragment.getLifecycle().addObserver(new LifecycleObserver() {
                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    public void onDestroy() {
                        parentFragment.getLifecycle().removeObserver(this);
                        DIALOG_POOL.remove(name);
                    }
                });
            } else {
                DIALOG_POOL.remove(name);
            }
        }
        return fragment;
    }
    
    /**
     * 创建构建器，链式调用
     */
    public static Builder with(@NonNull FragmentActivity activity) {
        Objects.requireNonNull(activity);
        return new Builder(activity);
    }
    
    /**
     * 创建构建器，链式调用
     */
    public static Builder with(@NonNull Fragment fragment) {
        Objects.requireNonNull(fragment);
        return new Builder(fragment);
    }
    
    public PhotoPreview(@NonNull Builder builder) {
        Objects.requireNonNull(builder);
        mFragmentActivity = builder.activity;
        mFragment = builder.fragment;
        mConfig = builder.mConfig;
    }
    
    /**
     * @param activity 当前图片预览所处Activity
     */
    public PhotoPreview(@NonNull FragmentActivity activity) {
        Objects.requireNonNull(activity);
        mFragmentActivity = activity;
        mFragment = null;
        mConfig = new Config();
    }
    
    /**
     * @param fragment 当前图片预览所处fragment
     */
    public PhotoPreview(@NonNull Fragment fragment) {
        Objects.requireNonNull(fragment);
        mFragmentActivity = null;
        mFragment = fragment;
        mConfig = new Config();
    }
    
    /**
     * 应用其它配置
     */
    public void setConfig(Config config) {
        mConfig.apply(config);
    }
    
    /**
     * 设置图片加载器
     */
    public void setImageLoader(ImageLoader imageLoader) {
        mConfig.imageLoader = imageLoader;
    }
    
    /**
     * 设置图片长按监听
     */
    public void setLongClickListener(OnLongClickListener listener) {
        mConfig.onLongClickListener = listener;
    }
    
    /**
     * 设置预览关闭监听
     */
    public void setOnDismissListener(OnDismissListener listener) {
        mConfig.onDismissListener = listener;
    }
    
    /**
     * 设置图片数量指示器样式，默认{@link IndicatorType#DOT},如果图片数量超过9，则不论设置何种模式，均为{@link IndicatorType#TEXT}
     */
    public void setIndicatorType(@IndicatorType int indicatorType) {
        mConfig.indicatorType = indicatorType;
    }
    
    /**
     * 多图预览时，当前预览的图片指示器颜色
     */
    public void setSelectIndicatorColor(@ColorInt int color) {
        mConfig.selectIndicatorColor = color;
    }
    
    /**
     * 多图预览时，非当前预览的图片指示器颜色
     */
    public void setNormalIndicatorColor(@ColorInt int color) {
        mConfig.normalIndicatorColor = color;
    }
    
    /**
     * 在调用{@link ImageLoader#onLoadImage(int, Object, ImageView)}时延迟展示loading框的时间，
     * < 0:不展示，=0:立即显示，>0:延迟给定时间显示，默认延迟100ms显示，如果在此时间内加载完成则不显示，否则显示
     */
    public void setDelayShowProgressTime(long delay) {
        mConfig.delayShowProgressTime = delay;
    }
    
    /**
     * 设置图片加载框的颜色，API >= 21配置才生效
     */
    public void setProgressColor(@ColorInt int progressColor) {
        mConfig.progressColor = progressColor;
    }
    
    /**
     * 设置图片加载框Drawable
     */
    public void setProgressDrawable(Drawable progressDrawable) {
        mConfig.progressDrawable = progressDrawable;
    }
    
    /**
     * 是否全屏预览，如果全屏预览，在某些手机上(特别是异形屏)可能会出全屏非全屏切换顿挫
     *
     * @param fullScreen <ul>
     *                   <li>null:跟随打开预览的Activity是否全屏决定预览界面是否全屏</li>
     *                   <li>true:全屏预览</li>
     *                   <li>null:非全屏预览</li>
     *                   </ul>
     */
    public void setFullScreen(Boolean fullScreen) {
        mConfig.fullScreen = fullScreen;
    }
    
    /**
     * 设置打开预览界面默认展示位置
     */
    public void setDefaultShowPosition(int position) {
        mConfig.defaultShowPosition = position;
    }
    
    /**
     * 设置图片地址
     */
    public void setSource(@NonNull Object... sources) {
        Objects.requireNonNull(sources);
        setSource(Arrays.asList(sources));
    }
    
    /**
     * 设置图片地址
     */
    public void setSource(@NonNull List<?> sources) {
        Objects.requireNonNull(sources);
        mConfig.sources = sources;
    }
    
    /**
     * 设置动画执行时间
     *
     * @param duration <ul>
     *                 <li>null: 使用默认动画时间</li>
     *                 <li><=0: 不执行动画</li>
     *                 </ul>
     */
    public void setAnimDuration(Long duration) {
        mConfig.animDuration = duration;
    }
    
    /**
     * 当{@link #setIndicatorType(int)}为{@link IndicatorType#DOT}时，设置DOT最大数量，
     * 如果{@link #setSource(List)}或{@link #setSource(Object...)}超出最大值，则采用{@link IndicatorType#TEXT}
     */
    public void setMaxIndicatorDot(int maxSize) {
        mConfig.maxIndicatorDot = maxSize;
    }
    
    /**
     * 设置缩略图图形变换类型，比如缩列图是圆形或圆角矩形
     *
     * @param shapeTransformType 目前仅提供{@link ShapeTransformType#CIRCLE}和{@link ShapeTransformType#ROUND_RECT}
     */
    public void setShapeTransformType(@ShapeTransformType int shapeTransformType) {
        mConfig.shapeTransformType = shapeTransformType;
    }
    
    /**
     * 仅当{@link #setShapeTransformType(int)}设置为{@link ShapeTransformType#ROUND_RECT}时，此值配置缩略图圆角矩形圆角半径
     */
    public void setShapeCornerRadius(int radius) {
        mConfig.shapeCornerRadius = radius;
    }
    
    /**
     * 是否展示缩略图蒙层,如果设置为{@code true},则预览动画执行时,缩略图不显示，预览更沉浸
     *
     * @param show 是否显示蒙层，默认{@code true}
     */
    public void setShowThumbnailViewMask(boolean show) {
        mConfig.showThumbnailViewMask = show;
    }
    
    /**
     * 是否在打开预览动画执行开始的时候执行状态栏隐藏/显示操作。如果该值设置为true，
     * 那么预览动画打开时，由于状态栏退出/进入有动画，可能导致预览动画卡顿(预览动画时间大于状态栏动画时间时发生)。
     *
     * @param doOP 是否执行操作，默认{@code false}
     */
    public void setOpenAnimStartHideOrShowStatusBar(boolean doOP) {
        mConfig.openAnimStartHideOrShowStatusBar = doOP;
    }
    
    // /**
    //  * 是否在关闭预览动画执行开始的时候执行状态栏显示/隐藏操作。如果该值设置为false，
    //  * 那么预览动画结束后，对于非沉浸式界面，由于要显示/隐藏状态栏，此时会有强烈的顿挫感。
    //  * 因此设置为{@code false}时，建议采用沉浸式
    //  *
    //  * @param doOP 是否执行操作，默认{@code true}
    //  */
    // public void setExitAnimStartHideOrShowStatusBar(boolean doOP) {
    //     mConfig.exitAnimStartHideOrShowStatusBar = doOP;
    // }
    
    /**
     * 多图预览时，左右滑动监听
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mConfig.onPageChangeListener = listener;
    }
    
    /**
     * 不设置缩略图，预览界面打开关闭将只有从中心缩放动画
     */
    public void show() {
        show((View) null);
    }
    
    /**
     * 展示预览
     *
     * @param thumbnailView 缩略图{@link View}，建议传{@link ImageView}对象，这样过度效果更好。
     *                      如果多图预览，请使用{@link #show(IFindThumbnailView)}
     */
    public void show(final View thumbnailView) {
        show(thumbnailView, null);
    }
    
    /**
     * 展示预览
     *
     * @param findThumbnailView 多图预览时，打开和关闭预览时用于提供缩略图对象，用于过度动画
     */
    public void show(final IFindThumbnailView findThumbnailView) {
        show(null, findThumbnailView);
    }
    
    private void show(final View thumbnailView, final IFindThumbnailView findThumbnailView) {
        correctConfig();
        final PreviewDialogFragment fragment
            = mFragment == null ? getDialog(Objects.requireNonNull(mFragmentActivity), true) : getDialog(mFragment, true);
        final Lifecycle lifecycle = mFragment == null ? mFragmentActivity.getLifecycle() : mFragment.getLifecycle();
        if (lifecycle.getCurrentState().isAtLeast(State.CREATED)) {
            Context context = mFragment == null ? mFragmentActivity : mFragment.getContext();
            FragmentManager fragmentManager
                = mFragment == null ? mFragmentActivity.getSupportFragmentManager() : mFragment.getChildFragmentManager();
            if (thumbnailView != null) {
                fragment.show(context, fragmentManager, mConfig, thumbnailView);
            } else {
                fragment.show(context, fragmentManager, mConfig, findThumbnailView);
            }
        } else if (lifecycle.getCurrentState() != State.DESTROYED) {
            lifecycle.addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Event.ON_CREATE)
                public void onCreate() {
                    lifecycle.removeObserver(this);
                    Context context = mFragment == null ? mFragmentActivity : mFragment.getContext();
                    FragmentManager fragmentManager
                        = mFragment == null ? mFragmentActivity.getSupportFragmentManager() : mFragment.getChildFragmentManager();
                    if (thumbnailView != null) {
                        fragment.show(context, fragmentManager, mConfig, thumbnailView);
                    } else {
                        fragment.show(context, fragmentManager, mConfig, findThumbnailView);
                    }
                }
            });
        }
    }
    
    /**
     * 纠正可能的错误配置
     */
    private void correctConfig() {
        int sourceSize = mConfig.sources == null ? 0 : mConfig.sources.size();
        if (sourceSize == 0) {
            mConfig.defaultShowPosition = 0;
        } else if (mConfig.defaultShowPosition >= sourceSize) {
            mConfig.defaultShowPosition = sourceSize - 1;
        } else if (mConfig.defaultShowPosition < 0) {
            mConfig.defaultShowPosition = 0;
        }
        
        if (mConfig.imageLoader == null) {
            mConfig.imageLoader = globalImageLoader;
        }
        
        if (mConfig.shapeTransformType != null
            && mConfig.shapeTransformType != ShapeTransformType.CIRCLE
            && mConfig.shapeTransformType != ShapeTransformType.ROUND_RECT) {
            mConfig.shapeTransformType = null;
        }
    }
    
    /**
     * 关闭预览界面
     */
    public void dismiss() {
        dismiss(true);
    }
    
    /**
     * 关闭预览界面
     *
     * @param callBack 是否需要执行{@link OnDismissListener}回调
     */
    public void dismiss(boolean callBack) {
        PreviewDialogFragment fragment
            = mFragment == null ? getDialog(Objects.requireNonNull(mFragmentActivity), false) : getDialog(mFragment, false);
        if (fragment != null) {
            fragment.dismiss(callBack);
        }
    }
    
    public static class Builder {
        final FragmentActivity activity;
        final Fragment fragment;
        Config mConfig;
        
        private Builder(FragmentActivity activity) {
            this.activity = activity;
            this.fragment = null;
            mConfig = new Config();
        }
        
        private Builder(Fragment fragment) {
            this.fragment = fragment;
            this.activity = null;
            mConfig = new Config();
        }
        
        /**
         * 应用其它配置
         */
        public Builder config(Config config) {
            mConfig.apply(config);
            return this;
        }
        
        /**
         * 图片加载器
         */
        public Builder imageLoader(ImageLoader imageLoader) {
            mConfig.imageLoader = imageLoader;
            return this;
        }
        
        /**
         * 多图预览时，指示器类型
         *
         * @param indicatorType {@link IndicatorType#DOT}、{@link IndicatorType#TEXT}
         */
        public Builder indicatorType(@IndicatorType int indicatorType) {
            mConfig.indicatorType = indicatorType;
            return this;
        }
        
        /**
         * 多图预览时，当前预览的图片指示器颜色
         */
        public Builder selectIndicatorColor(@ColorInt int color) {
            mConfig.selectIndicatorColor = color;
            return this;
        }
        
        /**
         * 多图预览时，非当前预览的图片指示器颜色
         */
        public Builder normalIndicatorColor(@ColorInt int color) {
            mConfig.normalIndicatorColor = color;
            return this;
        }
        
        /**
         * 设置图片加载loading drawable
         */
        public Builder progressDrawable(Drawable progressDrawable) {
            mConfig.progressDrawable = progressDrawable;
            return this;
        }
        
        /**
         * 设置图片加载loading颜色，该颜色作用于{@link #setProgressDrawable(Drawable)}上
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public Builder progressColor(@ColorInt int color) {
            mConfig.progressColor = color;
            return this;
        }
        
        /**
         * 在调用{@link ImageLoader#onLoadImage(int, Object, ImageView)}时延迟展示loading框的时间，
         * < 0:不展示，=0:立即显示，>0:延迟给定时间显示，默认延迟100ms显示，如果在此时间内加载完成则不显示，否则显示
         */
        public Builder delayShowProgressTime(long delay) {
            mConfig.delayShowProgressTime = delay;
            return this;
        }
        
        /**
         * 设置预览界面长按点检监听
         */
        public Builder onLongClickListener(OnLongClickListener listener) {
            mConfig.onLongClickListener = listener;
            return this;
        }
        
        /**
         * 设置预览关闭监听
         */
        public Builder onDismissListener(OnDismissListener listener) {
            mConfig.onDismissListener = listener;
            return this;
        }
        
        /**
         * 是否全屏预览，如果全屏预览，在某些手机上(特别是异形屏)可能会出全屏非全屏切换顿挫
         *
         * @param fullScreen <ul>
         *                   <li>null:跟随打开预览的Activity是否全屏决定预览界面是否全屏</li>
         *                   <li>true:全屏预览</li>
         *                   <li>null:非全屏预览</li>
         *                   </ul>
         */
        public Builder fullScreen(Boolean fullScreen) {
            mConfig.fullScreen = fullScreen;
            return this;
        }
        
        /**
         * 数据源
         */
        public Builder sources(@NonNull Object... sources) {
            Objects.requireNonNull(sources);
            return sources(Arrays.asList(sources));
        }
        
        /**
         * 数据源
         */
        public Builder sources(@NonNull List<?> sources) {
            Objects.requireNonNull(sources);
            mConfig.sources = sources;
            return this;
        }
        
        /**
         * 设置打开预览界面初始展示位置
         */
        public Builder defaultShowPosition(int position) {
            mConfig.defaultShowPosition = position;
            return this;
        }
        
        /**
         * 设置动画执行时间
         *
         * @param duration <ul>
         *                 <li>null: 使用默认动画时间</li>
         *                 <li><=0: 不执行动画</li>
         *                 </ul>
         */
        public Builder animDuration(Long duration) {
            mConfig.animDuration = duration;
            return this;
        }
        
        /**
         * 当{@link #indicatorType(int)}为{@link IndicatorType#DOT}时，设置DOT最大数量，
         * 如果{@link #sources(List)}或{@link #sources(Object...)}超出最大值，则采用{@link IndicatorType#TEXT}
         */
        public Builder maxIndicatorDot(int maxSize) {
            mConfig.maxIndicatorDot = maxSize;
            return this;
        }
        
        /**
         * 设置缩略图图形变换类型，比如缩列图是圆形或圆角矩形
         *
         * @param shapeTransformType 目前仅提供{@link ShapeTransformType#CIRCLE}和{@link ShapeTransformType#ROUND_RECT}
         */
        public Builder shapeTransformType(@ShapeTransformType int shapeTransformType) {
            mConfig.shapeTransformType = shapeTransformType;
            return this;
        }
        
        /**
         * 仅当{@link #shapeTransformType(int)}设置为{@link ShapeTransformType#ROUND_RECT}时，此值配置缩略图圆角矩形圆角半径
         */
        public Builder shapeCornerRadius(int radius) {
            mConfig.shapeCornerRadius = radius;
            return this;
        }
        
        /**
         * 是否展示缩略图蒙层,如果设置为{@code true},则预览动画执行时,缩略图不显示，预览更沉浸
         *
         * @param show 是否显示蒙层，默认{@code true}
         */
        public Builder showThumbnailViewMask(boolean show) {
            mConfig.showThumbnailViewMask = show;
            return this;
        }
        
        /**
         * 是否在打开预览动画执行开始的时候执行状态栏隐藏/显示操作。如果该值设置为true，
         * 那么预览动画打开时，由于状态栏退出/进入有动画，可能导致预览动画卡顿(预览动画时间大于状态栏动画时间时发生)。
         *
         * @param doOP 是否执行操作，默认{@code false}
         */
        public Builder openAnimStartHideOrShowStatusBar(boolean doOP) {
            mConfig.openAnimStartHideOrShowStatusBar = doOP;
            return this;
        }
        
        // /**
        //  * 是否在关闭预览动画执行开始的时候执行状态栏显示/隐藏操作。如果该值设置为false，
        //  * 那么预览动画结束后，对于非沉浸式界面，由于要显示/隐藏状态栏，此时会有强烈的顿挫感。
        //  * 因此设置为{@code false}时，建议采用沉浸式
        //  *
        //  * @param doOP 是否执行操作，默认{@code true}
        //  */
        // public Builder exitAnimStartHideOrShowStatusBar(boolean doOP) {
        //     mConfig.exitAnimStartHideOrShowStatusBar = doOP;
        //     return this;
        // }
        
        /**
         * 多图预览时，左右滑动监听
         */
        public Builder onPageChangeListener(OnPageChangeListener listener) {
            mConfig.onPageChangeListener = listener;
            return this;
        }
        
        public PhotoPreview build() {
            return new PhotoPreview(this);
        }
    }
}
