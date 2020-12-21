package com.wgw.photopreview;

import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Created by 汪高皖 on 2019/2/28 0028 14:21
 */
public class PhotoAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    
    private ScaleType mScaleType;
    private boolean mClipCircle;
    
    public PhotoAdapter(@Nullable List<String> data) {
        super(R.layout.item_img, data);
    }
    
    public PhotoAdapter(@Nullable List<String> data, ScaleType scaleType) {
        super(R.layout.item_img, data);
        mScaleType = scaleType;
    }
    
    public PhotoAdapter(@Nullable List<String> data, ScaleType scaleType, boolean clipCircle) {
        super(R.layout.item_img, data);
        mScaleType = scaleType;
        mClipCircle = clipCircle;
    }
    
    @Override
    protected void convert(BaseViewHolder helper, String item) {
        ImageView view = helper.getView(R.id.itemIv);
        ScaleType scaleType = mScaleType == null ? ScaleType.FIT_CENTER : mScaleType;
        view.setScaleType(scaleType);
        RequestOptions options;
        if (mClipCircle) {
            if (scaleType == ScaleType.CENTER_CROP) {
                options = new RequestOptions().transform(new CenterCrop(), new CircleCrop());
            } else if (scaleType == ScaleType.CENTER_INSIDE) {
                options = new RequestOptions().transform(new CenterInside(), new CircleCrop());
            } else {
                options = new RequestOptions().transform(new FitCenter(), new CircleCrop());
            }
        } else {
            options = new RequestOptions();
        }
        
        
        Glide.with(mContext)
            .load(item)
            .apply(options)
            // .override(Target.SIZE_ORIGINAL)
            .into(view);
    }
    
    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        notifyDataSetChanged();
    }
}
