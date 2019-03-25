package com.wgw.photo.preview;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnDismissListener;
import com.wgw.photo.preview.interfaces.OnLongClickListener;

import java.util.Arrays;
import java.util.List;

/**
 * 图片预览，支持预览单张，多张图片。预览界面是否全屏预览根据构造函数{@link PhotoPreview#PhotoPreview(AppCompatActivity, ImageLoader)}所传
 * Activity是否全屏决定。预览界面目前只能做到保持和Activity是否全屏一致的设置时顺滑打开和关闭预览界面，如果不一致，会导致打开关闭闪屏，特别是异形屏。
 * 后续加入单独设置预览界面是否全屏的设置。
 *
 * @author Created by 汪高皖 on 2019/2/26 0026 16:55
 */
public class PhotoPreview {
    private PreviewDialogFragment mDialogFragment;
    
    /**
     * @param activity    当前图片预览所处Activity
     * @param imageLoader 图片加载回调，图片加载逻辑需要外部自己实现
     */
    public PhotoPreview(@NonNull AppCompatActivity activity, @NonNull ImageLoader imageLoader) {
        mDialogFragment = new PreviewDialogFragment();
        mDialogFragment.setActivity(activity);
        mDialogFragment.setImageLoader(imageLoader);
    }
    
    public void setImageLoader(@NonNull ImageLoader imageLoader) {
        mDialogFragment.setImageLoader(imageLoader);
    }
    
    public void setLongClickListener(OnLongClickListener longClickListener) {
        mDialogFragment.setLongClickListener(longClickListener);
    }
    
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mDialogFragment.setOnDismissListener(onDismissListener);
    }
    
    public void setIndicatorType(@IndicatorType int indicatorType) {
        mDialogFragment.setIndicatorType(indicatorType);
    }
    
    public void setDelayShowProgressTime(long delayShowProgressTime) {
        mDialogFragment.setDelayShowProgressTime(delayShowProgressTime);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setProgressColor(int progressColor) {
        mDialogFragment.setProgressColor(progressColor);
    }
    
    public void setProgressDrawable(Drawable progressDrawable) {
        mDialogFragment.setProgressDrawable(progressDrawable);
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
        mDialogFragment.show(srcImageContainer, defaultShowPosition, picUrls);
    }
}
