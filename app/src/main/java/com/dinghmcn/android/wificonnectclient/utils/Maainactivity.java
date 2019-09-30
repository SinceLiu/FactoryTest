package com.dinghmcn.android.wificonnectclient.utils;



import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dinghmcn.android.wificonnectclient.R;

import java.lang.reflect.Method;

public class Maainactivity extends AppCompatActivity {
    public static final String[] REQUEST_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
    };
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏导航栏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_testmain);
        String[] permissions = REQUEST_PERMISSIONS;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e("lxx", ",申请权限:" + permission);
                ActivityCompat.requestPermissions(this, permissions, 0);
            }
        }
        mHandler.sendEmptyMessageDelayed(0, 1000);
        mHandler.sendEmptyMessage(1);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.e("lxx", "sensorManager: " + sensorManager + "    sensor:" + sensor);
        boolean succeed = sensorManager.registerListener(mListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        Log.e("lxx", "register：" + succeed);
        textView = (TextView) findViewById(R.id.content);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }


    //获取G550A校准、综测、耦合标志位
    private void getFlag() {
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        try {
            Class manager = telephonyManager.getClass();
            Method getDeviceCalibrationFlag = manager.getMethod("getDeviceCalibrationFlag", null);
            getDeviceCalibrationFlag.setAccessible(true);
            String flag = (String) getDeviceCalibrationFlag.invoke(telephonyManager, null);
            Log.e("lxx", "flag: " + flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //是否通话中
    public boolean isInCall() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK;
    }

    //挂电话
    public void endCall() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            Class telephonyManager = tm.getClass();
            Method getITelephony = telephonyManager.getDeclaredMethod("getITelephony", null);
            getITelephony.setAccessible(true);
            final Object obj = getITelephony.invoke(tm, null);
            Method endCall = obj.getClass().getMethod("endCall");
            endCall.setAccessible(true);
            endCall.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //辅助摄像头进光亮
    public int getAuxCameraBrightness() {
        try {
            @SuppressLint("WrongConstant") Object manager = getSystemService("rbci");
            Class rbciManager = manager.getClass();
            Method getAuxCameraBrightness = rbciManager.getMethod("GetAuxCameraBrightness");
            getAuxCameraBrightness.setAccessible(true);
            return (int) getAuxCameraBrightness.invoke(manager, null);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    //辅助摄像头进光亮
//        public int getAuxCameraBrightness() {
//            try {
//                Object manager = getSystemService("rbci");
//                Class rbciManager = manager.getClass();
//                Method getAuxCameraBrightness = rbciManager.getMethod("GetAuxCameraBrightness");
//                Method getIntByName = rbciManager.getMethod("RbciGetIntByName", String.class);
//                Method getBooleanByName = rbciManager.getMethod("RbciGetBooleanByName", String.class);
//                getIntByName.setAccessible(true);
//                getBooleanByName.setAccessible(true);
//                getAuxCameraBrightness.setAccessible(true);
//                Log.e("lxx", "getInt:" + getIntByName.invoke(manager, new String[]{"hall_switch"}) + "   getBoolean:" + getBooleanByName.invoke(manager, new String[]{"hall_switch"}));
//                return (int) getAuxCameraBrightness.invoke(manager, null);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return -1;
//            }
//        }


    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (isInCall()) {
                        Log.e("lxx", "在通话中，挂断电话");
                        endCall();
                    } else {
                        Log.e("lxx", "不在通话中");
                    }
                    break;
                case 1:
                    int brightness = getAuxCameraBrightness();
                    if (brightness > 0) {
                        textView.setText("辅助摄像头进光亮：" + brightness);
                        Log.e("lxx", "辅助摄像头进光亮:" + brightness);
                    } else {
                        Log.e("lxx", "获取进光亮失败");
                    }
                    mHandler.removeMessages(1);
                    mHandler.sendEmptyMessageDelayed(1, 1000);
                default:
                    break;
            }
        }
    };

    private SensorEventListener mListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float mValues[] = event.values;
            float val_x = (float) (Math.round((-mValues[0]) * 1000000)) / 1000000;
            float val_y = (float) (Math.round((-mValues[1]) * 1000000)) / 1000000;
            float val_z = (float) (Math.round((-mValues[2]) * 1000000)) / 1000000;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);

    }

}