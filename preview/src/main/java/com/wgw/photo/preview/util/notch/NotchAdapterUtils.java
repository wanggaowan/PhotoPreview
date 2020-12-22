package com.wgw.photo.preview.util.notch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * 全屏刘海屏适配
 * <ul>
 * <li><a href='https://dev.mi.com/console/doc/detail?pId=1293'>小米适配文档</a></li>
 * <li><a href='https://dev.vivo.com.cn/documentCenter/doc/103'>VIVO适配文档</a></li>
 * <li><a href='https://open.oppomobile.com/wiki/doc#id=10159'>OPPO适配文档</a></li>
 * <li><a href='https://developer.huawei.com/consumer/cn/devservice/doc/50114'>华为适配文档</a></li>
 * </ul>
 *
 * @author Created by 汪高皖 on 2019/3/12 0012 09:39
 */
public class NotchAdapterUtils {
    
    public static void adapter(Window window, @CutOutMode int cutOutMode) {
        if (window == null) {
            return;
        }
        
        if (!isNotch(window)) {
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            adapterP(window, cutOutMode);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            adapterO(window, cutOutMode);
        }
    }
    
    /**
     * 适配android P及以上系统
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private static void adapterP(Window window, @CutOutMode int cutOutMode) {
        if (window == null) {
            return;
        }
        
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.layoutInDisplayCutoutMode = cutOutMode;
        window.setAttributes(lp);
    }
    
    /**
     * 适配android O系统
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void adapterO(Window window, @CutOutMode int cutOutMode) {
        if (window == null) {
            return;
        }
        
        if (OSUtils.isMIUI()) {
            adapterOWithMIUI(window, cutOutMode);
        } else if (OSUtils.isEMUI()) {
            adapterOWithEMUI(window, cutOutMode);
        }
    }
    
    /**
     * 适配 MIUI android O系统
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void adapterOWithMIUI(Window window, @CutOutMode int cutOutMode) {
        if (window == null) {
            return;
        }
        
        /*
            0x00000100 开启配置
            0x00000200 竖屏配置
            0x00000400 横屏配置
            
            0x00000100 | 0x00000200 竖屏绘制到耳朵区
            0x00000100 | 0x00000400 横屏绘制到耳朵区
            0x00000100 | 0x00000200 | 0x00000400 横竖屏都绘制到耳朵区
         */
    
        int flag;
        if (cutOutMode == CutOutMode.ALWAYS) {
            flag = 0x00000100 | 0x00000200 | 0x00000400;
        } else {
            flag = 0x00000100 | 0x00000200;
        }
        
        String methodName;
        if (cutOutMode == CutOutMode.NEVER) {
            methodName = "clearExtraFlags";
        } else if (cutOutMode == CutOutMode.DEFAULT) {
            WindowManager.LayoutParams attributes = window.getAttributes();
            if ((attributes.flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) > 0) {
                methodName = "addExtraFlags";
            } else {
                methodName = "clearExtraFlags";
            }
        } else {
            methodName = "addExtraFlags";
        }
        
        try {
            Method method = Window.class.getMethod(methodName, int.class);
            method.invoke(window, flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 适配 EMUI android O系统
     */
    @SuppressWarnings({"unchecked"})
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void adapterOWithEMUI(Window window, @CutOutMode int cutOutMode) {
        if (window == null) {
            return;
        }
        
        int FLAG_NOTCH_SUPPORT = 0x00010000;
        String methodName;
        if (cutOutMode == CutOutMode.NEVER) {
            methodName = "clearHwFlags";
        } else if (cutOutMode == CutOutMode.DEFAULT) {
            WindowManager.LayoutParams attributes = window.getAttributes();
            if ((attributes.flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) > 0) {
                methodName = "addHwFlags";
            } else {
                methodName = "clearHwFlags";
            }
        } else {
            methodName = "addHwFlags";
        }
        
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        try {
            //noinspection rawtypes
            Class layoutParamsExCls = Class.forName("com.huawei.android.view.LayoutParamsEx");
            //noinspection rawtypes
            Constructor con = layoutParamsExCls.getConstructor(WindowManager.LayoutParams.class);
            Object layoutParamsExObj = con.newInstance(layoutParams);
            Method method = layoutParamsExCls.getMethod(methodName, int.class);
            method.invoke(layoutParamsExObj, FLAG_NOTCH_SUPPORT);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException
            | InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 是否是异形屏
     */
    public static boolean isNotch(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
        } else if (OSUtils.isMIUI()) {
            return isNotchOnMIUI();
        } else if (OSUtils.isEMUI()) {
            return isNotchOnEMUI(window.getContext());
        } else if (OSUtils.isVIVO()) {
            return isNotchOnVIVO(window.getContext());
        } else if (OSUtils.isOPPO()) {
            return isNotchOnOPPO(window.getContext());
        } else {
            return false;
        }
    }
    
    public static boolean isNotchOnMIUI() {
        return "1".equals(OSUtils.getProp("ro.miui.notch"));
    }
    
    @SuppressWarnings("unchecked")
    public static boolean isNotchOnEMUI(Context context) {
        if (context == null) {
            return false;
        }
        
        boolean isNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            //noinspection rawtypes
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchOnHuawei");
            isNotch = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
            //noinspection rawtypes
            @SuppressLint("PrivateApi")
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            isNotch = (boolean) method.invoke(FtFeature, VIVO_NOTCH);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
