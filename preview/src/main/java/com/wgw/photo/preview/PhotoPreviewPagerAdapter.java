package com.wgw.photo.preview;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

/**
 * @author Created by 汪高皖 on 2019/2/28 0028 11:22
 */
public class PhotoPreviewPagerAdapter extends BaseFragmentPagerAdapter {
    private int size;
    private OnUpdateFragmentDataListener mOnUpdateFragmentDataListener;
    private PhotoPreviewFragment.OnExitListener mFragmentOnExitListener;
    
    public PhotoPreviewPagerAdapter(FragmentManager fm, int size) {
        super(fm);
        this.size = size;
    }
    
    @Override
    public Fragment getItem(int position) {
        PhotoPreviewFragment fragment = new PhotoPreviewFragment();
        fragment.setOnExitListener(mFragmentOnExitListener);
        return fragment;
    }
    
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Object item = super.instantiateItem(container, position);
        if (item instanceof PhotoPreviewFragment) {
            if (mOnUpdateFragmentDataListener != null) {
                mOnUpdateFragmentDataListener.onUpdate((PhotoPreviewFragment) item, position);
            }
        }
        return item;
    }
    
    @Override
    public boolean dataIsChange(Object object) {
        return true;
    }
    
    @Override
    public int getCount() {
        return size;
    }
    
    public void setOnUpdateFragmentDataListener(OnUpdateFragmentDataListener onUpdateFragmentDataListener) {
        mOnUpdateFragmentDataListener = onUpdateFragmentDataListener;
    }
    
    public void setFragmentOnExitListener(PhotoPreviewFragment.OnExitListener fragmentOnExitListener) {
        mFragmentOnExitListener = fragmentOnExitListener;
    }
    
    public void setData(int size) {
        this.size = size;
        notifyDataSetChanged();
    }
    
    public interface OnUpdateFragmentDataListener {
        void onUpdate(PhotoPreviewFragment fragment, int position);
    }
}
