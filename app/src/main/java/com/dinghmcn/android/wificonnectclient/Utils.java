package com.dinghmcn.android.wificonnectclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.ScanResult;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static List<ScanResult> scanResults = new ArrayList<ScanResult>();
    private static boolean isWifiOpened;   //记录进入CIT前wifi的状态，退出CIT恢复

    public static void SetPreferences(Context context, SharedPreferences sp, int name, String flag) {
        String nameStr = context.getResources().getString(name);
        Editor editor = sp.edit();
        editor.putString(nameStr, flag);
        editor.apply();
    }

    public static void keepScreenOn(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    public static void hideSystemUi(Activity activity){
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); //隐藏系统状态栏，下拉显示，自动隐藏
    }

    public static List<ScanResult> getScanResults() {
        return scanResults;
    }

    public static void setScanResults(List<ScanResult> scanResults) {
        Utils.scanResults = scanResults;
    }

    public static boolean isWifiOpened() {
        return isWifiOpened;
    }

    public static void setIsWifiOpened(boolean isWifiOpened) {
        Utils.isWifiOpened = isWifiOpened;
    }
}
