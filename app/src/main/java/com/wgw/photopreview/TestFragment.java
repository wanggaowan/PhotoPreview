package com.wgw.photopreview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wgw.photo.preview.PhotoPreview;
import com.wgw.photo.preview.interfaces.ImageLoader;
import com.wgw.photo.preview.interfaces.OnDismissListener;

import java.util.Arrays;

/**
 * @author Created by 汪高皖 on 2019/3/11 0011 11:30
 */
public class TestFragment extends Fragment {
    private Context mContext;
    private int clickPosition;
    private Drawable clickPositionDrawable;
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main2, null);
        initView((RecyclerView) view.findViewById(R.id.rv));
        return view;
    }
    
    private void initView(final RecyclerView recyclerView){
        final String[] picData = new String[]{
            "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-704146.jpg",
            "http://pic1.nipic.com/2008-12-30/200812308231244_2.jpg",
            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1859156282,3890550903&fm=27&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=136331941,1945676606&fm=11&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=2770801839,2817170470&fm=26&gp=0.jpg"
        };
    
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        final PhotoAdapter adapter = new PhotoAdapter(Arrays.asList(picData));
        recyclerView.setAdapter(adapter);
    
        final PhotoPreview photoPreview = new PhotoPreview((AppCompatActivity) getActivity(), new ImageLoader() {
            @Override
            public void onLoadImage(int position, Object object, ImageView imageView) {
                Glide.with(TestFragment.this)
                    .load(((String) object))
                    .placeholder(position == clickPosition ? clickPositionDrawable : null)
                    .into(imageView);
            }
        });
        photoPreview.setDelayShowProgressTime(200);
    
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            photoPreview.setProgressColor(Color.WHITE);
        }
    
        photoPreview.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                Toast.makeText(mContext, "界面关闭", Toast.LENGTH_SHORT).show();
            }
        });
    
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ImageView imageView = view.findViewById(R.id.itemIv);
                clickPositionDrawable = imageView.getDrawable();
                clickPosition = position;
                photoPreview.show(recyclerView, position, Arrays.<Object>asList(picData));
            }
        });
    }
}
