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

import com.dinghmcn.android.wificonnectclient.CITTestHelper;

import java.util.Set;

import static android.app.ActivityThread.TAG;


/**
 * Created by zl121325 on 2019/4/11.
 */

public class BluetoothUtils {
    Context mContext;
    private BluetoothAdapter mBluetoothAdapter = null;
    private CITTestHelper application;
    private String bluetoothList = "";
    private Set<BluetoothDevice> bluetoothDevices;
    private Set<String> bluetoothDeviceAddresses;
    @SuppressLint("StaticFieldLeak")
    private static BluetoothUtils instance = null;

    private BluetoothUtils(Context mContext) {
        application = new CITTestHelper();
        this.mContext = mContext;
        registerReceiver();
    }

    public static BluetoothUtils getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new BluetoothUtils(context);
        }
        return instance;
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(blueToothReceiver, intentFilter);
    }

    private BroadcastReceiver blueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            assert action != null;
            switch (action) {
                case  BluetoothAdapter.ACTION_STATE_CHANGED:
                    if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                        if (!mBluetoothAdapter.isDiscovering()) {
                            mBluetoothAdapter.startDiscovery();
                        }
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    bluetoothList = "";
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!bluetoothDeviceAddresses.contains(device.getAddress())) {
                        Log.d(TAG, device.getName() + "---" + device.getAddress());
                        bluetoothDeviceAddresses.add(device.getAddress());
                        bluetoothDevices.add(device);
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    application.setBluetoothList(bluetoothList);
                    break;

                default:
            }
        }
    };

    public void bluetoothOpen() {
        Log.w(TAG, "BlueToothOpen: ");
        bluetoothDevices = new ArraySet<>();
        bluetoothDeviceAddresses = new ArraySet<>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        } else {
            assert mBluetoothAdapter != null;
            mBluetoothAdapter.startDiscovery();
        }
    }

    public Set<BluetoothDevice> getBluetoothDevices() {
        return bluetoothDevices;
    }

    public void exit() {
        mContext.unregisterReceiver(blueToothReceiver);
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON && !application.isBlueToothOpened()) {
            mBluetoothAdapter.disable();
            Log.e(TAG, "关闭蓝牙");
        }
    }
}
