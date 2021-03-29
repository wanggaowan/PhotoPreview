package com.wgw.photo.preview;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

/**
 * 预览图片适配器
 *
 * @author Created by wanggaowan on 3/24/21 11:17 AM
 */
class ImagePagerAdapter extends PagerAdapter {
    
    private final ShareData ShareData;
    private final PhotoPreviewHelper mHelper;
    
    public ImagePagerAdapter(PhotoPreviewHelper helper, ShareData shareData) {
        ShareData = shareData;
        mHelper = helper;
    }
    
    @Override
    public int getCount() {
        List<?> sources = ShareData.config.sources;
        return sources == null ? 0 : sources.size();
    }
    
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object instanceof ViewHolder && view == ((ViewHolder) object).root;
    }
    
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return new ViewHolder(mHelper, ShareData, container, position);
    }
    
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ViewHolder holder = (ViewHolder) object;
        holder.root.setTag(null);
        container.removeView(holder.root);
    }
    
    @Override
    public int getItemPosition(@NonNull Object object) {
        if (getCount() == 0) {
            return POSITION_NONE;
        }
        
        return POSITION_UNCHANGED;
    }
    
    static class ViewHolder {
        View root;
        private final PhotoView photoView;
        private final ProgressBar loading;
        
        private final PhotoPreviewHelper helper;
        private final ShareData shareData;
        // 记录预览界面图片缩放倍率为1时图片真实绘制大小
        private final float[] mNoScaleImageActualSize = new float[2];
        
        @SuppressLint("InflateParams")
        public ViewHolder(PhotoPreviewHelper helper, ShareData shareData, ViewGroup container, int position) {
            this.helper = helper;
            this.shareData = shareData;
            
            root = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_preview, container, false);
            container.addView(root);
            root.setTag(position);
            root.setTag(R.id.view_holder, this);
            
            photoView = root.findViewById(R.id.photoView);
            loading = root.findViewById(R.id.loading);
            
            photoView.setPhotoPreviewHelper(helper);
            photoView.setStartView(position == 0);
            List<?> sources = shareData.config.sources;
            int size = sources == null ? 0 : sources.size();
            photoView.setEndView(position == size - 1);
            
            initEvent();
            initLoading();
            loadImage(photoView, position);
        }
        
        private void initEvent() {
            photoView.setOnLongClickListener(v -> {
                if (shareData != null) {
                    OnLongClickListener listener = shareData.onLongClickListener;
                    if (listener != null) {
                        listener.onLongClick(root);
                    }
                }
                
                return true;
            });
            
            photoView.setOnClickListener(v -> helper.exit());
        }
        
        /**
         * 初始化loading
         */
        private void initLoading() {
            photoView.setOnMatrixChangeListener(this :: getPreviewDrawableSize);
            
            photoView.setImageChangeListener(drawable -> {
                if (drawable != null) {
                    loading.setVisibility(View.GONE);
                }
            });
            
            if (shareData.config.delayShowProgressTime < 0) {
                loading.setVisibility(View.GONE);
                return;
            }
            
            if (shareData.config.progressDrawable != null) {
                loading.setIndeterminateDrawable(shareData.config.progressDrawable);
            }
            
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && shareData.config.progressColor != null) {
                loading.setIndeterminateTintList(ColorStateList.valueOf(shareData.config.progressColor));
            }
            
            loading.setVisibility(shareData.config.delayShowProgressTime == 0 ? View.VISIBLE : View.GONE);
            if (shareData.config.delayShowProgressTime > 0) {
                // 监听指定延迟后图片是否加载成功
                photoView.postDelayed(() -> {
                    if (photoView.getDrawable() == null) {
                        loading.setVisibility(View.VISIBLE);
                    }
                }, shareData.config.delayShowProgressTime);
            }
        }
        
        /**
         * 获取预览图片真实大小，由于图片刚进入时，需要等待绘制，所以可能不能及时获取到准确的大小
         */
        private void getPreviewDrawableSize(RectF rectF) {
            if (photoView.getScale() != 1) {
                return;
            }
            
            // 用于退出时计算移动后最终图像坐标使用
            // 刚设置图片就获取，此时可能获取不成功
            mNoScaleImageActualSize[0] = rectF.width();
            mNoScaleImageActualSize[1] = rectF.height();
            if (mNoScaleImageActualSize[0] > 0) {
                // 计算最大缩放倍率，屏幕大小的三倍
                double ceil = Math.ceil(root.getWidth() / mNoScaleImageActualSize[0]);
                float maxScale = (float) (ceil * 3f);
                if (maxScale < photoView.getMaximumScale()) {
                    return;
                }
                
                float midScale = (maxScale + photoView.getMinimumScale()) / 2;
                photoView.setScaleLevels(photoView.getMinimumScale(), midScale, maxScale);
            }
        }
        
        /**
         * 加载图片
         */
        private void loadImage(ImageView imageView, int position) {
            if (shareData.config.imageLoader != null) {
                if (shareData.config.sources != null && position < shareData.config.sources.size() && position >= 0) {
                    shareData.config.imageLoader.onLoadImage(position, shareData.config.sources.get(position), imageView);
                } else {
                    shareData.config.imageLoader.onLoadImage(position, null, imageView);
                }
            }
        }
        
        public PhotoView getPhotoView() {
            return photoView;
        }
        
        public ProgressBar getLoading() {
            return loading;
        }
        
        public float[] getNoScaleImageActualSize() {
            return mNoScaleImageActualSize;
        }
    }
}
