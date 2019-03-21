package com.wgw.photo.preview.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.DisplayCutout;
import android.view.Window;
import android.view.WindowInsets;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 刘海屏工具
 *
 * @author Created by 汪高皖 on 2019/3/12 0012 09:39
 */
public class NotchUtils {
    /**
     * 是否是异形屏
     */
    public static boolean isNotch(Window window) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            boolean isNotchScreen = false;
            WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
            if (windowInsets != null) {
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                if (displayCutout != null) {
                    List<Rect> rects = displayCutout.getBoundingRects();
                    if (rects != null && rects.size() > 0) {
                        isNotchScreen = true;
                    }
                }
            }
            return isNotchScreen;
        } else if (OSUtils.isMiui()) {
            return isNotchOnMIUI();
        } else if (OSUtils.isEmui()) {
            return isNotchOnEMUI(window.getContext());
        } else if (OSUtils.isVivo()) {
            return isNotchOnVIVO(window.getContext());
        } else if (OSUtils.isOppo()) {
            return isNotchOnOPPO(window.getContext());
        } else {
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static boolean isNotchOnMIUI() {
        boolean isNotch = "1".equals(OSUtils.getProp("ro.miui.notch"));
        return isNotch;
    }
    
    @SuppressWarnings("unchecked")
    public static boolean isNotchOnEMUI(Context context) {
        if (context == null) {
            return false;
        }
        
        boolean isNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchOnHuawei");
            isNotch = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            //
        } catch (NoSuchMethodException e) {
            //
        } catch (Exception e) {
            //
        }
        return isNotch;
    }
    
    @SuppressWarnings("unchecked")
    public static boolean isNotchOnVIVO(Context context) {
        if (context == null) {
            return false;
        }
        
        // 是否有刘海
        int VIVO_NOTCH = 0x00000020;
        // 是否有圆角
        // int VIVO_FILLET = 0x00000008;
        boolean isNotch = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressLint("PrivateApi")
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            isNotch = (boolean) method.invoke(FtFeature, VIVO_NOTCH);
        } catch (ClassNotFoundException e) {
            //
        } catch (NoSuchMethodException e) {
            //
        } catch (Exception e) {
            //
        }
        return isNotch;
    }
    
    public static boolean isNotchOnOPPO(Context context) {
        if (context == null) {
            return false;
        }
    
        return context.getPackageManager()
            .hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }
}
