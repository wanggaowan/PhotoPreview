package com.wgw.photo.preview;

import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * 图片预览界面Adapter
 *
 * @author Created by wanggaowan on 2019/2/28 0028 11:22
 */
public class PhotoPreviewPagerAdapter extends FragmentPagerAdapter {
    
    final FragmentManager mFragmentManager;
    private final List<Object> mSources;
    private OnUpdateFragmentDataListener mOnUpdateFragmentDataListener;
    private PhotoPreviewFragment.OnExitListener mFragmentOnExitListener;
    
    public PhotoPreviewPagerAdapter(FragmentManager fm, List<Object> sources) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mSources = sources;
        mFragmentManager = fm;
    }
    
    @NonNull
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
    public int getCount() {
        return mSources == null ? 0 : mSources.size();
    }
    
    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
    
    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
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
