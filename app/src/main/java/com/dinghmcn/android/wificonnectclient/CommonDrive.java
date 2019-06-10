package com.dinghmcn.android.wificonnectclient;

public class CommonDrive {
	static {
		System.loadLibrary("CommonDrive");
	}

	public static native void flashlightControl(String string);

	public static native void backlightControl(int bBacklight);

	public static native void lightControl(int bLight);
        //Modify for CIT optimization by xiasiping 20140730 start
	public static native void lightControl_d(int bLight, String str_r, String str_o, String str_g);
        //Modify for CIT optimization by xiasiping 20140730 end
	public static native int calibration();
	
	public static native float[] getMotionXYZ();

	public static native int startWifiTestMode();

	public static native int stopWifiTestMode();

	public static native int InitWifiTestMode();

	public static native int deinitWifiTestMode();

	public static native int wifiStartRx(int iChannel);

	public static native int wifiStopRx();

	public static native int[] readwifiRxResult();

	public static native int[] readwifiRxStatus();

	public static native int wifiClearRxStatus();

	public static native int readWifiRxRssi();

	public static native int setRxEquipmentType(int nType);

	public static native int setRxTotalPacket(int nTotalPacket);

	public static native String getWifiMacAddress();

	public static native int setWifiMode(int iMode, int iRate);

	public static native int wifiSetTxGain(int iGain);

	public static native int wifiSetTxSIFS(int iSifs);

	public static native int wifiSetTxNumberofFrames(int nFrames);

	public static native int wifiSetPayLoadLength(int nPayLoad);

	public static native int wifiSetPreamble(int nPreamble);

	public static native int setWifiMacAddress(String strMacAddress);

	public static native int wifiStartTx(int iChannel);

	public static native int wifiStopTx();

	public static native void setCarrierSuppressionMode(int enable);

	public static native int runCommand(String cmd);

	public static native int[] getCompassValues();

	public static native int[] getOrientationValues();

	public static native String proximityDistance();

	public static native String lightDegree();

	public static native int getVoltage();

        //Modify for CIT optimization by xiasiping 20140730 start
	public static native int getVoltage_d(String str);
        //Modify for CIT optimization by xiasiping 20140730 end
	public static native int getCapacity();

	public static native int getHardwareVersion();

	public static native int proximityCalibration(int value);

	public static native int setButtonLight(boolean isOpen);

	// soft test
	public static native int wifiControl(int a, int b);

	public static native int bluetoothControl(boolean bBluetooth);

	public static native int openBluetooth();

	//MotionCalibrate
	public static native int[] excemotcal();

	public static native int execVcalibration();

	public static native void copyFile(String string, String string2);

	public static native int[] excemotcalH();

	public static native String getProximityPSwitch();
	
	public static native String getVersion();
	
	public static native int startUpgradeTouchPannelVersion();

	public static native int proximitySetCali(int which, int value);

	public static native String getHWVersion();
        //Modify for CIT optimization by xiasiping 20140730 start
	public static native String getHWVersion_d(String string1);

	public static native String getHWSubType();

	public static native String getHWSubType_d(String str);

        public static native void buttonlightControl(int value);

        public static native void buttonlightControl_d(int value, String node_buttonlight);

        public static native int getTemp();

        public static native int getTemp_d(String str);
        //Modify for CIT optimization by xiasiping 20140730 end
        public static native void lightControlForIS(int bLight);
        //Modify for P-sensor change in PA568 by xiasiping 20140919 start
        public static native int getXPsensor();

        public static native int getXPsensor_new();

        public static native int setPsensorNear(String str);
        public static native int setPsensorFar(String str);

        //Modify for P-sensor change in PA568 by xiasiping 20140919 end
        public static native int PtestHeartBeat();
        public static native int CtestHeartBeat();
        public static native String urtTtyTestOne(String str);
        public static native String urtTtyTestTwo(String str);

        public static native String GsensorCalibration();
        public static native int GyroCalibration();
        public static native int getGyro_x();
        public static native int getGyro_y();
        public static native int getGyro_z();

}

