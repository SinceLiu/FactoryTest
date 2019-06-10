package com.dinghmcn.android.wificonnectclient.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.dinghmcn.android.wificonnectclient.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static android.content.Context.BATTERY_SERVICE;

/**
 * Created by zl121325 on 2019/4/10.
 */

public class BatteryChargeUtils {
    private Context mContext;
    private String batteryStatus;
    private String quality;
    private boolean isChargingPass = false;
    private boolean isStatePass = false;
    private static BatteryChargeUtils instance = null;
    private int plugType;
    private int status;
    private int mLevel;
    private int voltage;
    private int temperature;
    BatteryManager batteryManager;
    private BatteryChargeUtils(Context mContext) {
        this.mContext=mContext;
         batteryManager = (BatteryManager)mContext.getSystemService(BATTERY_SERVICE);
        mContext.registerReceiver(mChargeInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    public static BatteryChargeUtils getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new BatteryChargeUtils(context);
        }
        return instance;
    }
    public void unregisterReceiver(){
        mContext.unregisterReceiver(mChargeInfoReceiver);
    }
    private BroadcastReceiver mChargeInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                 plugType = intent.getIntExtra("plugged", 0);///获取电源信息
                 status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);///获取电池状态
                 mLevel = intent.getIntExtra("level", 0);///电池剩余电量
                intent.getIntExtra("scale", 0);  ///获取电池满电量数值
                voltage =intent.getIntExtra("voltage", 0);  ///获取电池电压
                temperature=intent.getIntExtra("temperature", 0);  ///获取电池温度
                quality = " (" + mLevel + "%)";
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    /*if (plugType > 0) {
                        batteryStatus = getString(R.string.charging);
                        isChargingPass = true;
                    }*/
                    batteryStatus = mContext.getString(R.string.charging);
                    isChargingPass = true;
                } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    batteryStatus = mContext.getString(R.string.please_input_charger);
                    isChargingPass = false;
                } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                    batteryStatus = mContext.getString(R.string.please_input_charger);
                    isChargingPass = false;
                } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    batteryStatus = mContext.getString(R.string.battery_info_status_full);
                    isChargingPass = true;
                } else {
                    batteryStatus = "unknown state";
                    isChargingPass = false;
                }

                //modify for cit crash when charge test bug23462 by songguangyu 20140505 start
                /*if (isChargingPass && isStatePass){
                    btnPass.setEnabled(true);
                } else {
                    btnPass.setEnabled(false);
                }*/
                //modify for cit crash when charge test bug23462 by songguangyu 20140505 end
            }
        }
    };
    //当前充电电流 mA
    public int getCurrentChargingCurrent() {
        int result = 0;
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("/sys/class/power_supply/battery/BatteryAverageCurrent"));
            if ((line = br.readLine()) != null) {
                result = Integer.parseInt(line);
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (Build.VERSION.SDK_INT>21){
            return   batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        }
        return result;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public String getQuality() {
        return quality;
    }

    public int getPlugType() {
        return plugType;
    }

    public int getStatus() {
        return status;
    }

    public int getmLevel() {
        int battery = 0;
        if (Build.VERSION.SDK_INT>21)
             battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        if(mLevel!=0){
            return mLevel;
        }else {
           return battery;
        }
    }

    public int getVoltage() {
        return voltage;
    }

    public int getTemperature() {
        return temperature;
    }

    public boolean isChargingPass() {
        return isChargingPass;
    }
}
