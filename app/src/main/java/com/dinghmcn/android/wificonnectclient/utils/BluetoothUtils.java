package com.dinghmcn.android.wificonnectclient.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.Set;

import static android.app.ActivityThread.TAG;


/**
 * 获取蓝牙相关信息
 */
public class BluetoothUtils {
    /**
     * The M context.
     */
    Context mContext;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Set<BluetoothDevice> bluetoothDevices;
    private Set<String> bluetoothDeviceAddresses;

    public BluetoothUtils(Context mContext) {
        this.mContext = mContext;
        registerReceiver();
        bluetoothOpen();
    }

    // 监听蓝牙事件广播
    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        // 开始搜索蓝牙设置
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        // 蓝牙状态改变
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // 搜索蓝牙设置完成
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 发现蓝牙设备
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(blueToothReceiver, intentFilter);
        Log.e("CHEN", "startBlueToothLinster: " );
    }

    private BroadcastReceiver blueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("CHEN", "onReceiveBlueTooth: "+action );
            assert action != null;
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    // 蓝牙状态改变
                    if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                        // 蓝牙开启
                        if (!mBluetoothAdapter.isDiscovering()) {

                            mBluetoothAdapter.startDiscovery();
                        } else {
                            mBluetoothAdapter.startDiscovery();

                        }
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.e("czl", "ACTION_DISCOVERY_STARTED 6" + bluetoothDevices);
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    // 发现蓝牙设备
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // 保存发现的蓝牙设备，并根据MAC地址设备防止重复加入
                    if (!bluetoothDeviceAddresses.contains(device.getAddress())) {
                        Log.d(TAG, device.getName() + "---" + device.getAddress());
                        bluetoothDeviceAddresses.add(device.getAddress());
                        bluetoothDevices.add(device);
                        Log.e("czl", "ACTION_FOUND 6" + bluetoothDevices);
                    }
                    EventBus.getDefault().post(1011);
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.e("czl", "ACTION_DISCOVERY_FINISHED 6");
                    EventBus.getDefault().post(1011);
                    break;

                default:
            }
        }
    };


    /**
     * 开启蓝牙
     */
    public void bluetoothOpen() {
        Log.w(TAG, "BlueToothOpen: ");
        bluetoothDevices = new ArraySet<>();
        bluetoothDeviceAddresses = new ArraySet<>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            // 蓝牙未开启则开启蓝牙
            mBluetoothAdapter.enable();
        } else {
            // 蓝牙已开启则开始搜索蓝牙设备
            assert mBluetoothAdapter != null;
            mBluetoothAdapter.startDiscovery();
        }
    }

    /**
     * 重新开启蓝牙
     */
    public void RebluetoothOpen() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            // 蓝牙未开启则开启蓝牙
            mBluetoothAdapter.enable();
        } else {
            // 蓝牙已开启则开始搜索蓝牙设备
            assert mBluetoothAdapter != null;
            mBluetoothAdapter.startDiscovery();
        }
    }

    public void startSearchBlueTooth() {
        assert mBluetoothAdapter != null;
        //如果当前发现了新的设备，则停止继续扫描，当前扫描到的新设备会通过广播推向新的逻辑
//        if (mBluetoothAdapter.isDiscovering())
//            stopSearthBltDevice();
        mBluetoothAdapter.startDiscovery();
    }

    public boolean stopSearthBltDevice() {
        //暂停搜索设备
        assert mBluetoothAdapter != null;
        return mBluetoothAdapter.cancelDiscovery();
    }


    /**
     * 返回发现的蓝牙设备
     *
     * @return the bluetooth devices
     */
    public Set<BluetoothDevice> getBluetoothDevices() {
        return bluetoothDevices;
    }


    /**
     * 清理
     */
    public void exit() {
        mContext.unregisterReceiver(blueToothReceiver);
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            mBluetoothAdapter.disable();
            Log.e("czl", "关闭蓝牙");
        }
    }

}
