package com.dinghmcn.android.wificonnectclient.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;

import static android.content.ContentValues.TAG;

/**
 * The type Version utils.
 *
 * @author zl121325
 * @date 2019 /4/10
 */
public class VersionUtils {
    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static VersionUtils instance;
    private VersionUtils(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Get instance version utils.
     *
     * @param mContext the m context
     * @return the version utils
     */
    public static VersionUtils getInstance(Context mContext){
        if (instance==null){
            instance = new VersionUtils(mContext);
        }
        return instance;
    }


    private String getSystemPropertiesString(String property) {
        return SystemProperties.get(property, "unknown");
    }

    /**
     * Get serial number string.
     *
     * @return the string
     */
    public static String getSerialNumber(){

        String serial = null;

        try {

            Class<?> c =Class.forName("android.os.SystemProperties");

            Method get =c.getMethod("get", String.class);

            serial = (String)get.invoke(c, "ro.serialno");

        } catch (Exception e) {

            e.printStackTrace();

        }

        return serial;

    }
    private String getInternalVersion() {
        String internalVersion = null;

        String ivStr;
        if (internalVersion != null) {
            ivStr = getSystemPropertiesString(internalVersion);
        } else {
            ivStr = getSystemPropertiesString("ro.product.internaledition");
        }
        return ivStr;
    }
    private String getSECVersion() {
        String procCurrentStr;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/sys/devices/soc0/secure_boot_version"), 256);
            try {
                procCurrentStr = reader.readLine();
            } finally {
                reader.close();
            }
            Log.e("Version_getSECVersion", "getSECVersion()  = [" + procCurrentStr + "]");

            int isSec = Integer.parseInt(procCurrentStr);

            if (isSec == 1) {
                return "-sec";
            } else if (isSec == 0) {
                return "";
            }
            return "";
        } catch (Exception e) {
            Log.e("Version_getSECVersion", "getSECVersion() has Exception   = [" + e.toString() + "]");
            return "";
        }
    }


    private String getSoftwareVersion2() {
        String innerVersion = getInternalVersion() + getSECVersion();
        if (TextUtils.isEmpty(innerVersion) || innerVersion.contains("unknown")) {
            return android.os.Build.DISPLAY;
        } else {
            return innerVersion;
        }
    }

    /**
     * Get software version string.
     *
     * @return the string
     */
    public String getSoftwareVersion(){
        //Modify for CIT optimization by xiasiping 20140730 start
        String software_title = null;
        String software_time = null;
        String software_buildtime = null;

        String softwareTitle;
        String softwareTime = null;
        String softwareBuildTime;
        if (software_title != null) {
            Log.e(TAG, "xsp_software_title = " + software_title);
            softwareTitle = getSystemPropertiesString(software_title);
        } else {
            softwareTitle = getSystemPropertiesString("ro.product.version.software");
            Log.e(TAG, "xsp_software_title is null ");
        }
        if (software_time != null) {
            Log.e(TAG, "xsp_software_time = " + software_time);
            softwareTime = getSystemPropertiesString(software_title);
        } else {
            softwareTime = getSystemPropertiesString("ro.build.version.incremental");
            Log.e(TAG, "xsp_software_time is null");
        }
        if (software_buildtime != null) {
            Log.e(TAG, "xsp_software_buildtime = " + software_buildtime);
            softwareBuildTime = getSystemPropertiesString(software_buildtime);
        } else {
            softwareBuildTime = getSystemPropertiesString("ro.product.date");
            Log.e(TAG, "xsp_software_buildtime is null");
        }
        String head = softwareTitle + getSECVersion();
        if (TextUtils.isEmpty(head) || head.contains("unknown")) {
            head = getSoftwareVersion2();
        }
        softwareTitle = head + "_" + softwareBuildTime;

        return softwareTitle;

    }
}
