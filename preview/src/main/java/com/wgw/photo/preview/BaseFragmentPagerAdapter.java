package com.wgw.photo.preview;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;


/**
 * 可复用Fragment和动态调整界面数量。解决动态调整数据大小时，当数据从非0转化为0时，采用系统逻辑时界面还保留一个界面Bug<br>
 * 完整复制FragmentPagerAdapter代码并加入方法{@link #dataIsChange(Object)}用于判断当前界面是否可以重用
 *
 * @author Created by 汪高皖 on 2018/9/27 0027 09:45
 */
@SuppressWarnings("WeakerAccess")
public abstract class BaseFragmentPagerAdapter extends PagerAdapter {
    
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;
    private Fragment mCurrentPrimaryItem = null;
    
    public BaseFragmentPagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }
    
    /**
     * Return the Fragment associated with a specified position.
     */
    public abstract Fragment getItem(int position);
    
    /**
     * Fragment数据是否发生变化，这个数据变化指的是相同的界面布局，
     * 但是获取界面数据的重要参数发生变化，此时则需要重新进行界面刷新
     */
    public abstract boolean dataIsChange(Object object);
    
    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        if (container.getId() == View.NO_ID) {
            throw new IllegalStateException("ViewPager with adapter " + this
                + " requires a view id");
        }
    }
    
    @NonNull
    @SuppressLint("CommitTransaction")
    @SuppressWarnings("ReferenceEquality")
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        
        final long itemId = getItemId(position);
        // Do we already have this fragment?
        String name = makeFragmentName(container.getId(), itemId);
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            mCurTransaction.attach(fragment);
        } else {
            fragment = getItem(position);
            mCurTransaction.add(container.getId(), fragment,
                makeFragmentName(container.getId(), itemId));
        }
        
        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        } else if (dataIsChange(fragment)) {
            mCurrentPrimaryItem = null;
        }
        
        return fragment;
    }
    
    @SuppressLint("CommitTransaction")
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        mCurTransaction.detach((Fragment) object);
    }
    
    @SuppressWarnings("ReferenceEquality")
    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            
            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);
            mCurrentPrimaryItem = fragment;
        }
    }
    
    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitNowAllowingStateLoss();
            mCurTransaction = null;
        }
    }
    
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return ((Fragment) object).getView() == view;
    }
    
    @Override
    public Parcelable saveState() {
        return null;
    }
    
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }
    
    /**
     * Return a unique identifier for the item at the given position.
     *
     * <p>The default implementation returns the given position.
     * Subclasses should override this method if the positions of items can change.</p>
     *
     * @param position Position within this adapter
     * @return Unique identifier for the item at position
     */
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getItemPosition(@NonNull Object object) {
        if (getCount() == 0) {
            // 如果数据为0，则必须返回POSITION_NONE，如果使用系统逻辑，
            // 当数据从非0变为0时，还会有一个界面存在于Viewpager中
            return POSITION_NONE;
        }
        
        return dataIsChange(object) ? POSITION_NONE : POSITION_UNCHANGED;
    }
    
    public String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }
}
