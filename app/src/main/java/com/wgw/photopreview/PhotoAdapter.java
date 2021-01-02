package com.wgw.photopreview;

import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wgw.photo.preview.util.Utils;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Created by 汪高皖 on 2019/2/28 0028 14:21
 */
public class PhotoAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    
    private ScaleType mScaleType;
    private Boolean mClipCircle;
    
    public PhotoAdapter(@Nullable List<String> data) {
        super(R.layout.item_img, data);
    }
    
    public PhotoAdapter(@Nullable List<String> data, ScaleType scaleType) {
        super(R.layout.item_img, data);
        mScaleType = scaleType;
    }
    
    public PhotoAdapter(@Nullable List<String> data, ScaleType scaleType, Boolean clipCircle) {
        super(R.layout.item_img, data);
        mScaleType = scaleType;
        mClipCircle = clipCircle;
    }
    
    @Override
    protected void convert(BaseViewHolder helper, String item) {
        ImageView view = helper.getView(R.id.itemIv);
        LayoutParams layoutParams = view.getLayoutParams();
        if (mClipCircle == null || !mClipCircle) {
            layoutParams.width = LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.width = Utils.dp2px(view.getContext(), 100);
        }
        
        ScaleType scaleType = mScaleType == null ? ScaleType.FIT_CENTER : mScaleType;
        view.setScaleType(scaleType);
        RequestOptions options;
        if (mClipCircle != null) {
            if (mClipCircle) {
                if (scaleType == ScaleType.CENTER_CROP) {
                    options = new RequestOptions().transform(new CenterCrop(), new CircleCrop());
                } else if (scaleType == ScaleType.CENTER_INSIDE) {
                    options = new RequestOptions().transform(new CenterInside(), new CircleCrop());
                } else {
                    options = new RequestOptions().transform(new FitCenter(), new CircleCrop());
                }
            } else {
                if (scaleType == ScaleType.CENTER_CROP) {
                    options = new RequestOptions().transform(new CenterCrop(), new RoundedCorners(100));
                } else if (scaleType == ScaleType.CENTER_INSIDE) {
                    options = new RequestOptions().transform(new CenterInside(), new RoundedCorners(100));
                } else {
                    options = new RequestOptions().transform(new FitCenter(), new RoundedCorners(100));
                }
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
