package com.dinghmcn.android.wificonnectclient.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.dinghmcn.android.wificonnectclient.MyXml;
import com.dinghmcn.android.wificonnectclient.MyXmlUtils;
import com.dinghmcn.android.wificonnectclient.R;
import com.dinghmcn.android.wificonnectclient.XProperty;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 *
 * @author zl121325
 * @date 2019/4/10
 */

public class VersionUtils {
    private Context mContext;
    private List<XProperty> xProperties;
    private String model = null;
    private String baseBand = null;
    @SuppressLint("StaticFieldLeak")
    private static VersionUtils instance;
    private VersionUtils(Context mContext) {
        this.mContext = mContext;
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        initEvent();
    }
    public static VersionUtils getInstance(Context mContext){
        if (instance==null){
            instance = new VersionUtils(mContext);
        }
        return instance;
    }
    private void initEvent() {
        try {
            MyXml mxml = new MyXmlUtils().getMxml();
            xProperties = mxml.getXProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (xProperties != null) {
            for (XProperty xpp : xProperties) {
                String name = xpp.getName();
                Log.w(TAG, "initEvent: "+name );
                if ("model".equals(name)) {
                    model = xpp.getValue();
                }
                if ("baseband".equals(name)) {
                    baseBand = xpp.getValue();
                }
            }
        }
    }

    private String getSystemPropertiesString(String property) {
        return SystemProperties.get(property, "unknown");
    }

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
        if (xProperties != null) {
            for (XProperty xpp : xProperties) {
                String name = xpp.getName();
                if ("internaledition".equals(name)) {
                    internalVersion = xpp.getValue();
                }
            }
        }

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

    public String getHardwareVersion(){
     return SystemProperties.get(baseBand, mContext.getResources().getString(R.string.cit_info_default));
    }

    public String getSoftwareVersion(){
        //Modify for CIT optimization by xiasiping 20140730 start
        String software_title = null;
        String software_time = null;
        String software_buildtime = null;
        if (xProperties != null) {
            for (XProperty xpp : xProperties) {
                String name = xpp.getName();
                if ("software_title".equals(name)) {
                    software_title = xpp.getValue();
                }
                if ("software_time".equals(name)) {
                    software_time = xpp.getValue();
                }
                if ("software_buildtime".equals(name)) {
                    software_buildtime = xpp.getValue();
                }

            }
        }

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
