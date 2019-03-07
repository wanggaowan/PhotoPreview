package com.wgw.photopreview;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * @author Created by 汪高皖 on 2019/3/7 0007 08:44
 */
public class MyApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
