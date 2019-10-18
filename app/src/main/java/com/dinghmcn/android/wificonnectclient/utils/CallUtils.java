package com.dinghmcn.android.wificonnectclient.utils;

import android.content.Context;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


import com.android.internal.telephony.ITelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.content.Context.TELEPHONY_SERVICE;

public class CallUtils {
    private Context mContext;
    TelephonyManager manager;
    boolean mFlag1 = false, mFlag2 = false, mFlag3 = false;

    public CallUtils(Context mContext) {
        this.mContext = mContext;
        getCallService();
    }

    private void getCallService() {
        manager = (TelephonyManager) mContext
                .getSystemService(TELEPHONY_SERVICE);
        manager.listen(new MyPhoneListener(),
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class MyPhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.w("onCallStateChanged: ", "1111");
                    Toast.makeText(mContext, "已经接通", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ITelephony telephonyService;
                            TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
//                            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
//                            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
//                                    Class c = Class.forName(telephony.getClass().getName());
//                                    Method m = c.getDeclaredMethod("getITelephony");
//                                    m.setAccessible(true);
//                                    //java.lang.ClassCastException: com.android.internal.telephony.ITelephony$Stub$Proxy cannot be cast to com.dinghmcn.android.wificonnectclient.ITelephony
//                                    telephonyService = (ITelephony) m.invoke(telephony);
//                                    telephonyService.endCall();
                                Class telephonyManager = telephony.getClass();
                                Method getDeviceCalibrationFlag = telephonyManager.getMethod("getDeviceCalibrationFlag");
                                getDeviceCalibrationFlag.setAccessible(true);
                                String flag = (String) getDeviceCalibrationFlag.invoke(telephony);
                                Log.e("lxx", "flag: " + flag);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1000);
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, phoneNumber);
        }
    }
}
