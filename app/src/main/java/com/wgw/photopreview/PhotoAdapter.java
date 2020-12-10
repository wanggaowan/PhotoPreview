package com.wgw.photopreview;

import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Created by 汪高皖 on 2019/2/28 0028 14:21
 */
public class PhotoAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    
    private ScaleType mScaleType;
    
    public PhotoAdapter(@Nullable List<String> data) {
        super(R.layout.item_img, data);
    }
    
    public PhotoAdapter(@Nullable List<String> data, ScaleType scaleType) {
        super(R.layout.item_img, data);
        mScaleType = scaleType;
    }
    
    @Override
    protected void convert(BaseViewHolder helper, String item) {
        ImageView view = helper.getView(R.id.itemIv);
        view.setScaleType(mScaleType == null ? ScaleType.FIT_CENTER : mScaleType);
        Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(view);
    }
    
    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        notifyDataSetChanged();
    }
}
