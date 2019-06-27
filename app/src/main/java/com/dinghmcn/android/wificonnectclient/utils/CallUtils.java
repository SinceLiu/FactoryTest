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
    private static CallUtils instance;
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
//    public String getCalibrationFlag(){
//        String flag = manager.getDeviceCalibrationFlag();
//        Log.v("testResult", "testResult__flag = " + flag);
//        String flag1 = flag.substring(0, 1);
//        mFlag1 = flag1.equalsIgnoreCase("P");
//        String flag2 = flag.substring(1, 2);
//        mFlag2 = flag2.equalsIgnoreCase("P");
//        String flag3 = flag.substring(2, 3);
//        mFlag3 = flag3.equalsIgnoreCase("P");
//    String testResult = "";
//        if(mFlag1)
//    {
//        testResult = testResult + "校准：" + "PASS" + "\n";
//    }
//        else
//    {
//        testResult = testResult + "校准：" + "FAIL" + "\n";
//    }
//
//        if(mFlag2)
//    {
//        testResult = testResult + "综测：" + "PASS" + "\n";
//    }
//        else
//    {
//        testResult = testResult + "综测：" + "FAIL" + "\n";
//    }
//
//        if(mFlag3)
//    {
//        testResult = testResult + "耦合：" + "PASS";
//    }
//        else
//    {
//        testResult = testResult + "耦合：" + "FAIL";
//    }
//       return testResult;
//    }
//
//    private String getDeviceCalibrationFlag(){
//        String status = null;
//        try {
//            Class cls = Class.forName("android.telephony.TelephonyManager");
//            try {
////                TelephonyManager obj = (TelephonyManager) cls.newInstance();
//                try {
//                    Method meth = cls.getMethod("getDeviceCalibrationFlag");
//                    meth.setAccessible(true);
//                    try {
//                        status = (String) meth.invoke(cls.newInstance());
//                    } catch (InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                }
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return status;
//    }

    public static CallUtils getInstance(Context mContext){
        if (instance==null)
            instance=new CallUtils(mContext);
        return instance;
    }
    private class MyPhoneListener extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            switch (state){
                    case  TelephonyManager.CALL_STATE_IDLE:
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.w( "onCallStateChanged: ","1111" );
                        Toast.makeText(mContext,"已经接通",Toast.LENGTH_SHORT).show();
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
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        },1000);

                            break;
            }
            super.onCallStateChanged(state, phoneNumber);
        }
    }
}
