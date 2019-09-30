package com.dinghmcn.android.wificonnectclient.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.util.Log;


public final class RbciManager {

    private static final String TAG = "<rbci_Manager>";

    private Context mContext;
    private IRbciService mService;

	public static final String RBCI_TRUE = "1";
	public static final String RBCI_FALSE = "0";

    public static final String CAMERA_ALS = "camera_als";
    public static final String MAIN_CAMERA_ALS = "main_camera_als";
    public static final String AUX_CAMERA_ALS = "aux_camera_als";
	public static final String AUX_CAMERA_SWITCH = "aux_camera_switch";

	public static final String VOICE_WAKE_UP_HW_TEST = "v_wakeup_hw_test";


    public static final int DISCONNECTED = -2;
    
    public RbciManager(Context context, IRbciService service) {
		mContext = context;
		mService = service;
    }
    

    
	public String GetCameraInfo() {
		if (mService == null) {
    		Log.w(TAG, "GetCameraInfo IRbciService is null");
			return null;
    	}
		return mService.IRbciGetInfo(CAMERA_ALS);
	}
	
	public int RbciGetIntByName(String rbci_name) {
		if (mService == null) {
			Log.e(TAG, "RbciGetIntByName IRbciService is null");
			return -1;
		}
		String read_str = mService.IRbciRead(rbci_name);
		try {
			int parse_int = Integer.parseInt(read_str);
			return parse_int;
		}catch (NumberFormatException e){
			Log.e(TAG, "RbciGetIntByName Integer.parseInt error, read_str:" + read_str);
			return -1;
		}
	}

	public boolean RbciGetBooleanByName(String rbci_name) {
		if (mService == null) {
			Log.e(TAG, "RbciGetBooleanByName IRbciService is null");
			return false;
		}
		String read_str = mService.IRbciRead(rbci_name);
		if(read_str.equals(RBCI_TRUE))
		{
			return true;
		}else{
			return false;
		}

	}

	public int RbciSetBooleanByName(String rbci_name, boolean status) {
		if (mService == null) {
			Log.e(TAG, "RbciSetBooleanByName IRbciService is null");
			return -1;
		}
		if(status)
		{
			return mService.IRbciwrite(rbci_name, RBCI_TRUE);
		}else{
			return mService.IRbciwrite(rbci_name, RBCI_FALSE);
		}
	}

	
	public int GetMainCameraBrightness() {
		return RbciGetIntByName(MAIN_CAMERA_ALS);
	}


	public int GetAuxCameraBrightness() {
		return RbciGetIntByName(AUX_CAMERA_ALS);
	}

	public int OpenAuxCamera() {
		return RbciSetBooleanByName(AUX_CAMERA_SWITCH, true);
	}

	public int CloseAuxCamera() {
		return RbciSetBooleanByName(AUX_CAMERA_SWITCH, false);
	}

	public boolean GetAuxCameraOpenStatus() {
		return RbciGetBooleanByName(AUX_CAMERA_SWITCH);
	}


	public boolean VoiceWakeUpHardwareTest() {
		if (mService == null) {
    		Log.w(TAG, "VoiceWakeUpHardwareTest IRbciService is null");
			return false;
    	}
		String read_str = mService.IRbciGetInfo(VOICE_WAKE_UP_HW_TEST);
		if(read_str != null)
		{
			return true;
		}else{
			return false;
		}
	}

	
}

