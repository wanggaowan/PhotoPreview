package com.wgw.photo.preview;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;

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
    private final PreviewDialogFragment mDialogFragment;
    
    /**
     * @param activity    当前图片预览所处Activity
     * @param imageLoader 图片加载回调，图片加载逻辑需要外部自己实现
     */
    public PhotoPreview(@NonNull AppCompatActivity activity, @NonNull ImageLoader imageLoader) {
        mDialogFragment = new PreviewDialogFragment();
        mDialogFragment.setActivity(activity);
        mDialogFragment.setImageLoader(imageLoader);
    }
    
    /**
     * 设置图片加载器
     */
    public void setImageLoader(@NonNull ImageLoader imageLoader) {
        mDialogFragment.setImageLoader(imageLoader);
    }
    
    /**
     * 设置图片长按监听
     */
    public void setLongClickListener(OnLongClickListener longClickListener) {
        mDialogFragment.setLongClickListener(longClickListener);
    }
    
    /**
     * 设置预览关闭监听
     */
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mDialogFragment.setOnDismissListener(onDismissListener);
    }
    
    /**
     * 设置图片数量指示器样式，默认{@link IndicatorType#DOT},如果图片数量超过9，则不论设置何种模式，均为{@link IndicatorType#TEXT}
     */
    public void setIndicatorType(@IndicatorType int indicatorType) {
        mDialogFragment.setIndicatorType(indicatorType);
    }
    
    /**
     * 在调用{@link ImageLoader#onLoadImage(int, Object, ImageView)}时延迟展示loading框的时间，
     * < 0:不展示，=0:立即显示，>0:延迟给定时间显示，默认延迟100ms显示，如果在此时间内加载完成则不显示，否则显示
     */
    public void setDelayShowProgressTime(long delayShowProgressTime) {
        mDialogFragment.setDelayShowProgressTime(delayShowProgressTime);
    }
    
    /**
     * 设置图片加载框的颜色，默认loading样式为转动的圆圈
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setProgressColor(int progressColor) {
        mDialogFragment.setProgressColor(progressColor);
    }
    
    /**
     * 设置图片加载框Drawable，可指定loading样式，颜色等
     */
    public void setProgressDrawable(Drawable progressDrawable) {
        mDialogFragment.setProgressDrawable(progressDrawable);
    }
    
    /**
     * @param srcImageContainer 源视图，可以是{@link AbsListView}、{@link RecyclerView}或{@link View}，
     *                          如果是{@link AbsListView}或{@link RecyclerView}，请确保children数量 >= picUrls size。
     *                          且保持ITEM内容只有图片最佳，因为在打开和关闭图片预览时，均以ITEM整体视图为源视图进行缩放动画。
     *                          所以如果不是只有图片内容，那么缩放动画就会发生偏移，此时建议单张单张预览，源视图传ITEM中的ImageView，
     *                          不建议整体预览
     * @param picUrls           图片Url数据，该数据决定可预览图片的数量
     */
    public void show(View srcImageContainer, @NonNull Object... picUrls) {
        show(srcImageContainer, Arrays.asList(picUrls));
    }
    
    /**
     * @param srcImageContainer 源视图，可以是{@link AbsListView}、{@link RecyclerView}或{@link View}，
     *                          如果是{@link AbsListView}或{@link RecyclerView}，请确保children数量 >= picUrls size。
     *                          且保持ITEM内容只有图片最佳，因为在打开和关闭图片预览时，均以ITEM整体视图为源视图进行缩放动画。
     *                          所以如果不是只有图片内容，那么缩放动画就会发生偏移，此时建议单张单张预览，源视图传ITEM中的ImageView，
     *                          不建议整体预览
     * @param picUrls           图片Url数据，该数据决定可预览图片的数量
     */
    public void show(View srcImageContainer, @NonNull List<?> picUrls) {
        show(srcImageContainer, 0, picUrls);
    }
    
    /**
     * @param srcImageContainer   源视图，可以是{@link AbsListView}、{@link RecyclerView}或{@link View}，
     *                            如果是{@link AbsListView}或{@link RecyclerView}，请确保children数量 >= picUrls size。
     *                            且保持ITEM内容只有图片最佳，因为在打开和关闭图片预览时，均以ITEM整体视图为源视图进行缩放动画。
     *                            所以如果不是只有图片内容，那么缩放动画就会发生偏移，此时建议单张单张预览，源视图传ITEM中的ImageView，
     *                            不建议整体预览
     * @param defaultShowPosition 如果预览多张照片，此数据默认打开图片位置
     * @param picUrls             图片Url数据，该数据决定可预览图片的数量
     */
    public void show(@NonNull View srcImageContainer, int defaultShowPosition, @NonNull Object... picUrls) {
        show(srcImageContainer, defaultShowPosition, Arrays.asList(picUrls));
    }
    
    /**
     * @param srcImageContainer   源视图，可以是{@link AbsListView}、{@link RecyclerView}或{@link View}，
     *                            如果是{@link AbsListView}或{@link RecyclerView}，请确保children数量 >= picUrls size。
     *                            且保持ITEM内容只有图片最佳，因为在打开和关闭图片预览时，均以ITEM整体视图为源视图进行缩放动画。
     *                            所以如果不是只有图片内容，那么缩放动画就会发生偏移，此时建议单张单张预览，源视图传ITEM中的ImageView，
     *                            不建议整体预览
     * @param defaultShowPosition 如果预览多张照片，此数据默认打开图片位置
     * @param picUrls             图片Url数据，该数据决定可预览图片的数量
     */
    public void show(@NonNull View srcImageContainer, int defaultShowPosition, @NonNull List<?> picUrls) {
        mDialogFragment.show(srcImageContainer, defaultShowPosition, picUrls);
    }
    
    /**
     * 关闭预览界面
     */
    public void dismiss() {
        mDialogFragment.dismiss(true);
    }
    
    /**
     * 关闭预览界面
     *
     * @param callBack 是否需要执行{@link OnDismissListener}回调
     */
    public void dismiss(boolean callBack) {
        mDialogFragment.dismiss(callBack);
    }
}
