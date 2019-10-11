package com.dinghmcn.android.wificonnectclient.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.dinghmcn.android.wificonnectclient.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static android.content.Context.BATTERY_SERVICE;

/**
 * 获取电池相关信息
 *
 * @author zl121325
 * @date 2019/4/10
 */
public class BatteryChargeUtils {
    private Context mContext;
    private String batteryStatus;
    private String quality;
    private boolean isChargingPass = false;
    private int plugType;
    private int status;
    private int mLevel;
    private int voltage;
    private int temperature;
    private BatteryManager batteryManager;

    public BatteryChargeUtils(Context mContext) {
        this.mContext = mContext;
        batteryManager = (BatteryManager) mContext.getSystemService(BATTERY_SERVICE);
        // 注册电池事件监听器
        mContext.registerReceiver(mChargeInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    /**
     * 移除监听器
     */
    public void unregisterReceiver() {
        mContext.unregisterReceiver(mChargeInfoReceiver);
    }

    private BroadcastReceiver mChargeInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                //获取电源信息
                plugType = intent.getIntExtra("plugged", 0);
                //获取电池状态
                status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                //电池剩余电量
                mLevel = intent.getIntExtra("level", 0);
                //获取电池满电量数值
                intent.getIntExtra("scale", 0);
                //获取电池电压
                voltage = intent.getIntExtra("voltage", 0);
                //获取电池温度
                temperature = intent.getIntExtra("temperature", 0);
                quality = " (" + mLevel + "%)";
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
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
            }
        }
    };

    /**
     * 当前充电电流 mA
     *
     * @return the current charging current
     */

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

        if (Build.VERSION.SDK_INT > 21) {
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        }
        return result;
    }

    /**
     * 获取电池状态
     *
     * @return the battery status
     */
    public String getBatteryStatus() {
        return batteryStatus;
    }

    /**
     * Gets quality.
     *
     * @return the quality
     */
    public String getQuality() {
        return quality;
    }

    /**
     * //获取电源信息
     *
     * @return the plug type
     */
    public int getPlugType() {
        return plugType;
    }

    /**
     * 获取电池状态
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * 获取电量
     *
     * @return the level
     */
    public int getmLevel() {
        int battery = 0;
        if (Build.VERSION.SDK_INT > 21) {
            battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        }
        if (mLevel != 0) {
            return mLevel;
        } else {
            return battery;
        }
    }

    /**
     * 获取电池电压
     *
     * @return the voltage
     */
    public int getVoltage() {
        return voltage;
    }

    /**
     * 获取电池温度
     *
     * @return the temperature
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * 是否在充电
     *
     * @return the boolean
     */
    public boolean isChargingPass() {
        return isChargingPass;
    }
}
