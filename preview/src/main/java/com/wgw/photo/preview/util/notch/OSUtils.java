package com.wgw.photo.preview.util.notch;


import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OSUtils {
    
    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_SMARTISAN = "SMARTISAN";
    public static final String ROM_VIVO = "VIVO";
    
    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_SMARTISAN = "ro.smartisan.version";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";
    
    private static String sName;
    private static String sVersion;
    
    public static boolean isEMUI() {
        return check(ROM_EMUI);
    }
    
    public static boolean isMIUI() {
        return check(ROM_MIUI);
    }
    
    public static boolean isMI6() {
        boolean check = check(ROM_MIUI);
        if (check) {
            return "MI 6".equals(Build.MODEL);
        }
        
        return false;
    }
    
    public static boolean isVIVO() {
        return check(ROM_VIVO);
    }
    
    public static boolean isOPPO() {
        return check(ROM_OPPO);
    }
    
    public static String getName() {
        if (sName == null) {
            check("");
        }
        return sName;
    }
    
    public static String getVersion() {
        if (sVersion == null) {
            check("");
        }
        return sVersion;
    }
    
    public static boolean check(String rom) {
        if (sName != null) {
            return sName.equals(rom);
        }
        
        if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_MIUI))) {
            sName = ROM_MIUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_EMUI))) {
            sName = ROM_EMUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_OPPO))) {
            sName = ROM_OPPO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_VIVO))) {
            sName = ROM_VIVO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_SMARTISAN))) {
            sName = ROM_SMARTISAN;
        } else {
            sVersion = Build.DISPLAY;
            if (sVersion.toUpperCase().contains(ROM_FLYME)) {
                sName = ROM_FLYME;
            } else {
                sVersion = Build.UNKNOWN;
                sName = Build.MANUFACTURER.toUpperCase();
            }
        }
        return sName.equals(rom);
    }
    
    public static String getProp(String name) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }
}


