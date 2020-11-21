package com.wgw.photo.preview;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.wgw.photo.preview.interfaces.IFindThumbnailView;
import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnDismissListener;
import com.wgw.photo.preview.interfaces.OnLongClickListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.RecyclerView;

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
    private static final Map<String, PreviewDialogFragment> DIALOG_POOL = new HashMap<>();
    
    private final FragmentActivity mFragmentActivity;
    private Config mConfig;
    
    public static void setGlobalImageLoader(ImageLoader imageLoader) {
        globalImageLoader = imageLoader;
    }
    
    private static PreviewDialogFragment getDialog(final FragmentActivity activity, boolean noneCreate) {
        final String name = activity.toString();
        PreviewDialogFragment fragment = DIALOG_POOL.get(name);
        if (fragment == null) {
            if (noneCreate) {
                fragment = new PreviewDialogFragment();
                DIALOG_POOL.put(name, fragment);
                activity.getLifecycle().addObserver(new LifecycleObserver() {
                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    public void onDestroy() {
                        DIALOG_POOL.remove(name);
                        activity.getLifecycle().removeObserver(this);
                    }
                });
            }
        }
        return fragment;
    }
    
    /**
     * 创建构建器，链式调用
     */
    public static Builder with(@NonNull FragmentActivity activity) {
        assert activity != null;
        return new Builder(activity);
    }
    
    public PhotoPreview(@NonNull Builder builder) {
        assert builder != null;
        mFragmentActivity = builder.activity;
        mConfig = builder.mConfig;
    }
    
    /**
     * @param activity 当前图片预览所处Activity
     */
    public PhotoPreview(@NonNull FragmentActivity activity) {
        assert activity != null;
        mFragmentActivity = activity;
        mConfig = new Config();
        mConfig.imageLoader = globalImageLoader;
    }
    
    /**
     * 应用其它配置
     */
    public void setConfig(Config config) {
        if (config != null) {
            mConfig = config;
        }
    }
    
    /**
     * 设置图片加载器
     */
    public void setImageLoader(ImageLoader imageLoader) {
        mConfig.imageLoader = imageLoader;
        if (mConfig.imageLoader == null) {
            mConfig.imageLoader = globalImageLoader;
        }
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
        assert sources != null;
        setSource(Arrays.asList(sources));
    }
    
    /**
     * 设置图片地址
     */
    public void setSource(@NonNull List<Object> sources) {
        assert sources != null;
        mConfig.sources = sources;
    }
    
    /**
     * 不设置缩略图，预览界面打开关闭将只有从中心缩放动画
     */
    public void show() {
        show((View) null);
    }
    
    /**
     * @param thumbnailView 缩略图，可以是{@link AbsListView}、{@link RecyclerView}、{@link ViewGroup}或{@link View}，
     *                      如果是{@link AbsListView}或{@link RecyclerView}，建议ITEM内容只有图片控件，因为在打开和关闭图片预览时，
     *                      以ITEM整体视图为缩略图进行过度动画，所以如果不是只有图片内容，那么过度动画效果可能不佳，此时建议使用{@link #show(IFindThumbnailView)}
     */
    public void show(final View thumbnailView) {
        final PreviewDialogFragment fragment = getDialog(mFragmentActivity, true);
        if (mFragmentActivity.getLifecycle().getCurrentState().isAtLeast(State.CREATED)) {
            fragment.show(mFragmentActivity, mConfig, thumbnailView);
        } else if (mFragmentActivity.getLifecycle().getCurrentState() != State.DESTROYED) {
            mFragmentActivity.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Event.ON_CREATE)
                public void onCreate() {
                    fragment.show(mFragmentActivity, mConfig, thumbnailView);
                    mFragmentActivity.getLifecycle().removeObserver(this);
                }
            });
        }
    }
    
    /**
     * @param findThumbnailView 在打开和关闭预览时提供缩略图对象，用于过度动画。对于预览{@link AbsListView}或{@link RecyclerView}中的对象很有帮助。
     *                          因为列表中的Item往往都不是只是图片控件，还有很多其它对象，此时缩略图只想指定Item中的图片控件，那么使用该对象就很容易实现。
     */
    public void show(final IFindThumbnailView findThumbnailView) {
        final PreviewDialogFragment fragment = getDialog(mFragmentActivity, true);
        if (mFragmentActivity.getLifecycle().getCurrentState().isAtLeast(State.CREATED)) {
            fragment.show(mFragmentActivity, mConfig, findThumbnailView);
        } else if (mFragmentActivity.getLifecycle().getCurrentState() != State.DESTROYED) {
            mFragmentActivity.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Event.ON_CREATE)
                public void onCreate() {
                    fragment.show(mFragmentActivity, mConfig, findThumbnailView);
                    mFragmentActivity.getLifecycle().removeObserver(this);
                }
            });
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
        PreviewDialogFragment fragment = getDialog(mFragmentActivity, false);
        if (fragment != null) {
            fragment.dismiss(callBack);
        }
    }
    
    public static class Builder {
        final FragmentActivity activity;
        Config mConfig;
        
        private Builder(FragmentActivity activity) {
            this.activity = activity;
            mConfig = new Config();
            mConfig.imageLoader = globalImageLoader;
        }
        
        /**
         * 应用其它配置
         */
        public Builder config(Config config) {
            if (config != null) {
                mConfig = config;
            }
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
            assert sources != null;
            return sources(Arrays.asList(sources));
        }
        
        /**
         * 数据源
         */
        public Builder sources(@NonNull List<Object> sources) {
            assert sources != null;
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
        
        public PhotoPreview build() {
            return new PhotoPreview(this);
        }
    }
}
