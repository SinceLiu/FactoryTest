
package com.dinghmcn.android.wificonnectclient;

import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;



public class AllTest extends Activity {
//    WiFiTools mWifiTools;

    List<ScanResult> mWifiList = null;

    boolean mWifiConReslut = false;

    boolean mWifiResult = false;

    boolean mWifiStatus = false;

    boolean mOtherOk = false;

    boolean mBlueResult = false;

    boolean mBlueFlag = false;

    boolean mBlueStatus = false;

    boolean mSdCardResult = false;
    
    boolean mGPSResult = false;

    Message msg = null;

    SharedPreferences mSp;

    private BluetoothAdapter mAdapter = null;

    boolean isregisterReceiver = false;

    HandlerThread mBlueThread = new HandlerThread("blueThread");

    BlueHandler mBlueHandler;

    HandlerThread mWifiThread = new HandlerThread("wifiThread");

    WifiHandler mWifiHandler;
    public static boolean begin_auto_test=false;
    // GPSThread mGPS = null; //bob

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alltest);
        begin_auto_test = true;
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
//        mWifiTools = new WiFiTools(this);

        /*mWifiThread.start();
        mWifiHandler = new WifiHandler(mWifiThread.getLooper());
        mWifiHandler.post(wifirunnable);

        mBlueThread.start();
        mBlueHandler = new BlueHandler(mBlueThread.getLooper());
        mBlueHandler.post(bluerunnable);*/

        // mGPS = new GPSThread(this); //bob
        // mGPS.start();

        /*Intent intent = new Intent();
        intent.setClassName(this, "com.mediatek.factorymode.BatteryLog");
        this.startActivityForResult(intent, AppDefine.FT_BATTERYID);*/
        
        Intent intent = new Intent();
        intent.setClassName(this, "com.mediatek.factorymode.touchscreen.LineTest");
        this.startActivityForResult(intent, AppDefine.FT_TOUCHSCREENID);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent();
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); //bob.chen disabled
        int requestid = -1;
        
        if (requestCode == AppDefine.FT_TOUCHSCREENID) {
            intent.setClassName(this, "com.mediatek.factorymode.lcd.LCD");
            requestid = AppDefine.FT_LCDID;
        }
        if (requestCode == AppDefine.FT_LCDID) {
            intent.setClassName(this, "com.mediatek.factorymode.BatteryLog");
            requestid = AppDefine.FT_BATTERYID;
        }
//        if (requestCode == AppDefine.FT_BATTERYID) {
//            if (resultCode == RESULT_FIRST_USER) {
//                finish();
//                return;
//            }
//            intent.setClassName(this, "com.mediatek.factorymode.AdcCheck");
//            requestid = AppDefine.FT_ADC;
//        }
        if (requestCode == AppDefine.FT_BATTERYID) {
            intent.setClassName(this, "com.mediatek.factorymode.audio.AudioTest");
            requestid = AppDefine.FT_AUDIOID;
        }
        if (requestCode == AppDefine.FT_AUDIOID) {
            intent.setClassName(this, "com.mediatek.factorymode.microphone.MicRecorder");
            requestid = AppDefine.FT_FMRADIOID;
        }
        if (requestCode == AppDefine.FT_FMRADIOID) {
            intent.setClassName(this, "com.mediatek.factorymode.wifi.WiFiTest");
            requestid = AppDefine.FT_WIFIID;
        }
        if (requestCode == AppDefine.FT_WIFIID) {
            intent.setClassName(this, "com.mediatek.factorymode.bluetooth.Bluetooth");
            requestid = AppDefine.FT_BLUETOOTHID;
        }
        if (requestCode == AppDefine.FT_BLUETOOTHID) {
            intent.setClassName(this, "com.mediatek.factorymode.vibrator.Vibrator");
            requestid = AppDefine.FT_VIBRATORID;
        }
        if (requestCode == AppDefine.FT_VIBRATORID) {
            intent.setClassName(this, "com.mediatek.factorymode.signal.Signal");
            requestid = AppDefine.FT_SIGNALID;
        }
//        if (requestCode == AppDefine.FT_SIGNALID) {
//            intent.setClassName(this, "com.mediatek.factorymode.backlight.BackLight");
//            requestid = AppDefine.FT_BACKLIGHTID;
//        }
        if (requestCode == AppDefine.FT_BACKLIGHTID) {
            intent.setClassName(this, "com.mediatek.factorymode.simcard.SimCard");
            requestid = AppDefine.FT_SIMSDCARDHOOKID;
        }
//        if (requestCode == AppDefine.FT_SIMSDCARDHOOKID) {
//            intent.setClassName(this, "com.mediatek.factorymode.gps.GPS");
//            requestid = AppDefine.FT_GPSID_2;
//        }
//        if (requestCode == AppDefine.FT_GPSID) {
//            intent.setClassName(this, "com.mediatek.factorymode.gps.GPS2");
//            requestid = AppDefine.FT_GPSID_2;
//        }
//        if (requestCode == AppDefine.FT_GPSID_2) {
//            intent.setClassName(this, "com.mediatek.factorymode.sensor.GSensorWithCalibrate");
//            requestid = AppDefine.FT_GSENSORID_2;
//        }
//        if (requestCode == AppDefine.FT_GSENSORID_2) {
//            intent.setClassName(this, "com.mediatek.factorymode.sensor.GSensor");
//            requestid = AppDefine.FT_GSENSORID;
//        }
        if (requestCode == AppDefine.FT_GSENSORID) {
            intent.setClassName(this, "com.mediatek.factorymode.camera.CameraTest");
            requestid = AppDefine.FT_DEVICEINFO;
        }
        if (requestCode == AppDefine.FT_DEVICEINFO) {
            intent.setClassName(this, "com.mediatek.factorymode.DeviceInfo");
            requestid = AppDefine.FT_CAMERAID;
        }
        if (requestCode == AppDefine.FT_CAMERAID) {
            OnFinish();
            return;
        }
        
		
        this.startActivityForResult(intent, requestid);
    }

    public void onDestroy() {
        super.onDestroy();
        /*BackstageDestroy();*/
    }

    public void BackstageDestroy() {

        mBlueHandler.removeCallbacks(bluerunnable);
//        mWifiHandler.removeCallbacks(wifirunnable);
        if (isregisterReceiver == true) {
            unregisterReceiver(mReceiver);
        }
        mAdapter.disable();
        // mGPS.closeLocation(); //bob
    }

    public void SdCardInit() {
        String sDcString = Environment.getExternalStorageState();
        if (sDcString.equals(Environment.MEDIA_MOUNTED)) {
            mSdCardResult = true;
        }
    }

//    public boolean WifiInit() {
//
//
//    }

//    Runnable wifirunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (mWifiStatus == false) {
//                boolean res = WifiInit();
//                if (res == false) {
//                } else {
//                    mWifiStatus = true;
//                }
//                mWifiHandler.postDelayed(this, 3000);
//            } else {
//                if (mWifiTools.IsConnection()) {
//                    mWifiResult = true;
//                    mWifiTools.closeWifi();
//                } else {
//                    mWifiHandler.postDelayed(this, 3000);
//                }
//            }
//        }
//    };

    class WifiHandler extends Handler {
        public WifiHandler() {
        }

        public WifiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    public void BlueInit() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.enable();
        if (mAdapter.isEnabled() == true) {
            StartReciver();
            while (mAdapter.startDiscovery() == false) {
                mAdapter.startDiscovery();
            }
        } else {
            mBlueHandler.postDelayed(bluerunnable, 3000);
        }
    }

    public void StartReciver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        isregisterReceiver = true;
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mBlueResult = true;
                    if (isregisterReceiver == true) {
                        unregisterReceiver(mReceiver);
                        isregisterReceiver = false;
                    }
                    mAdapter.disable();
                }
            }
        }
    };

    Runnable bluerunnable = new Runnable() {
        @Override
        public void run() {
            BlueInit();
        }
    };

    class BlueHandler extends Handler {
        public BlueHandler() {
        }

        public BlueHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    public void OnFinish() {
        // Utils.SetPreferences(this, mSp, R.string.memory_name, AppDefine.FT_SUCCESS);  genju.chen disable
        // Utils.SetPreferences(this, mSp, R.string.gps_name, //bob
        // (mGPS.isSuccess()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        /*Utils.SetPreferences(this, mSp, R.string.wifi_name,
                (mWifiResult == true) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        Utils.SetPreferences(this, mSp, R.string.bluetooth_name,
                (mBlueResult == true) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);*/
        AllTest.begin_auto_test = false;
        finish();
    }
}
