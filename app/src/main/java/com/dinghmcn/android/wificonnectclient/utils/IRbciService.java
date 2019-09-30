package com.dinghmcn.android.wificonnectclient.utils;
import android.app.Service;
import android.content.Context;

import android.util.Slog;

public class IRbciService  {
	private static final String TAG = "IRbciService";

	IRbciService() {
		Slog.i(TAG, "services/core/java/com/android/server  Rbci init");
		init_native();
		Slog.i(TAG, "services/core/java/com/android/server Rbci init ok");
	}
	

	public String IRbciGetInfo(String name) {
		return GetInfo_native(name);
	}

	public String IRbciRead(String name) {
		return Read_native(name);
	}

	public int IRbciwrite(String name, String data) {
		return Write_native(name, data);
	}

	public String IRbciGetList() {
		return IRbciGetList();
	}

	private static native boolean init_native();	
	
	private static native String GetInfo_native(String name);
	private static native String Read_native(String name);	
	private static native int Write_native(String name, String data);
	private static native String GetList_native();

}
