package com.dinghmcn.android.wificonnectclient.utils;

import android.content.Context;

import java.lang.reflect.Method;

import static android.content.Context.CAMERA_SERVICE;


public class AuxiliaryCameraUtils {


    private Context mContext;
    private static AuxiliaryCameraUtils instance;

    public AuxiliaryCameraUtils(Context mContext) {
        this.mContext = mContext;
        startAuxiliaryCameraUtils();
    }

    private int startAuxiliaryCameraUtils() {
//            IRbciService mIBbciServer;
//            RbciManager rbciManager = (RbciManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            return 2;
//                Object manager = getSystemService("rbci");
//
//                Class RbciManager = rbciManager.getClass();
//                Method getAuxCameraBrightness = RbciManager.getMethod("GetAuxCameraBrightness");
//                getAuxCameraBrightness.setAccessible(true);
//                return (int) getAuxCameraBrightness.invoke(RbciManager,null);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private class CAMERA_ALS {
    }
}
