package com.wgw.photo.preview;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

/**
 * 图片预览界面Adapter
 *
 * @author Created by 汪高皖 on 2019/2/28 0028 11:22
 */
public class PhotoPreviewPagerAdapter extends BaseFragmentPagerAdapter {
    
    private final FragmentManager mFragmentManager;
    private int size;
    private OnUpdateFragmentDataListener mOnUpdateFragmentDataListener;
    private PhotoPreviewFragment.OnExitListener mFragmentOnExitListener;
    
    public PhotoPreviewPagerAdapter(FragmentManager fm, int size) {
        super(fm);
        this.size = size;
        mFragmentManager = fm;
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
    
    /**
     * 查找指定位置的fragment
     */
    public Fragment findFragment(ViewPager viewPager, int position) {
        String name = makeFragmentName(viewPager.getId(), getItemId(position));
        return mFragmentManager.findFragmentByTag(name);
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
    
    /**
     * 预览界面更改监听
     */
    public interface OnUpdateFragmentDataListener {
        /**
         * 预览界面更新
         *
         * @param fragment 当前预览界面
         * @param position 当前预览对象位置
         */
        void onUpdate(PhotoPreviewFragment fragment, int position);
    }
}
