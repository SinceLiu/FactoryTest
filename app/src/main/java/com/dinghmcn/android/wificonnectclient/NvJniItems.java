package com.dinghmcn.android.wificonnectclient;

import android.util.Log;
import android.support.annotation.NonNull;

/**
 * Created by 1 on 2017/2/22.
 */

public class NvJniItems {
    private static native void diagNvRead(NvInfo nvInfo);
    private static native void diagNvWrite(NvInfo nvInfo);
    public static native void diagReadImei(NvInfo nvInfo, int index);

    static final String TAG = NvJniItems.class.getSimpleName();
    private static volatile NvJniItems sInstance = null;

    private NvJniItems(){
        System.loadLibrary("readboy_nv");
    }

    public static NvJniItems getInstance(){
        if(sInstance == null){
            synchronized (NvJniItems.class){
                if(sInstance == null){
                    sInstance = new NvJniItems();
                }
            }
        }
        return sInstance;
    }

    private boolean checkNvId(int nvId){
        return NvItemIds.nvItemIds.get(nvId, false);
    }

    private byte[] getSubData(byte[] data, int startPos){
        int realDataLength = data.length - startPos;
        if(realDataLength > 0){
            byte[] realData = new byte[realDataLength];
            System.arraycopy(data, startPos, realData, 0, realDataLength);
            return realData;
        }
        return null;
    }

    public byte[] readNv(@NonNull NvInfo nvInfo){
        if(checkNvId(nvInfo.nvID)) {
            diagNvRead(nvInfo);
            return getSubData(nvInfo.data, 3);
        }else {
            Log.e(TAG, "nvId is not allow to read");
            new IllegalArgumentException();
        }
        return null;
    }

    public boolean writeNv(@NonNull NvInfo nvInfo){
        if(checkNvId(nvInfo.nvID)) {
            diagNvWrite(nvInfo);
            return true;
        }else {
            Log.e(TAG, "nvId is not allow to write");
            new IllegalArgumentException();
            return false;
        }
    }

    public boolean writeNv2499(byte[] data){
        if(data != null) {
            NvInfo nvInfo = new NvInfo(NvItemIds.NV_FACTORY_DATA_3_I);
            nvInfo.data = data;
            diagNvWrite(nvInfo);
            return true;
        }else {
            Log.e(TAG, "nvId is not allow to write");
            new IllegalArgumentException();
            return false;
        }
    }

    /**
     * get nv 2499
     * @return @return byte[] or null
     */
    public byte[] getNv2499(){
        NvInfo nvInfo = new NvInfo(NvItemIds.NV_FACTORY_DATA_3_I);
        diagNvRead(nvInfo);
        return getSubData(nvInfo.data, 3);
    }

    /**
     * get nv 2497
     * @return byte[] or null
     */
    public byte[] getNv2497(){
        NvInfo nvInfo = new NvInfo(NvItemIds.NV_FACTORY_DATA_1_I);
        diagNvRead(nvInfo);
        return getSubData(nvInfo.data, 3);
    }

    public String getWifiMac(){
        NvInfo nvInfo = new NvInfo(NvItemIds.NV_WIFI_MAC_I);
        diagNvRead(nvInfo);
        return parseWifiMac(nvInfo.data);
    }

    /**
     * get meid from nv 1943
     * @return meid or null
     */
    public String getMeid(){
        NvInfo nvInfo = new NvInfo(NvItemIds.NV_MEID_I);
        diagNvRead(nvInfo);
        return parseMeid(nvInfo.data);
    }

    /**
     * get imei from nv 550
     * @param index imei index
     * @return imei or null
     */
    public String getImei(int index){
        if(index != 0 || index != 1){
            new IllegalArgumentException();
        }
        NvInfo nvInfo = new NvInfo(NvItemIds.NV_UE_IMEI_I);
        diagReadImei(nvInfo, index);
        return parseImei(nvInfo.data);
    }

    @NonNull
    private String parseImei(byte[] data){
        byte[] realData = getSubData(data, 8);
        if(realData != null && realData.length > 8){
            int index = 0;
            int[] imeiArray = new int[15];
            StringBuilder imeiBuilder = new StringBuilder();
            // read byte values, and swap byte order
            for (int tempIndex = 1; tempIndex <=8; tempIndex++) {
                //do this for everything execpt last byte
                if (tempIndex != 8) {
                    imeiArray[index] = realData[tempIndex];
                    imeiArray[index] &= 0xF0;
                    imeiArray[index] = imeiArray[index] >> 4;
                    imeiArray[index + 1] = realData[tempIndex + 1] & 0x0F;

                }
                else { // last byte
                    imeiArray[index] = realData[tempIndex];
                    imeiArray[index] &= 0xF0;
                    imeiArray[index] = imeiArray[index] >> 4;
                }

                index = index + 2;
            }

            // iterate thryu the array, and build up the components that make up IMEI
            for (int i = 0; i < imeiArray.length; i++) {
                imeiBuilder.append(String.valueOf(imeiArray[i]));
            }
            return imeiBuilder.toString();
        }
        return null;
    }

    @NonNull
    private String parseMeid(byte[] data){
        byte[] realData = getSubData(data, 3);
        if(realData != null && realData.length > 6) {
            String[] stringArray = new String[18];
            StringBuilder meidBuilder = new StringBuilder(14);
            StringBuilder rrBuilder = new StringBuilder(2);
            StringBuilder macBuilder = new StringBuilder(6);
            StringBuilder snrBuilder = new StringBuilder(6);

            int i = 0;
            for (int index = 6; index >= 0; index--) {
                int a = realData[index];
                int b = realData[index]  & 0xF;
                int c = realData[index] >> 4;
                stringArray[i + 1] = Integer.toHexString(realData[index] & 0xF).toUpperCase();
                stringArray[i] = Integer.toHexString((realData[index] >> 4) & 0xF).toUpperCase();

                if (index == 6) {
                    rrBuilder.append(stringArray[i]);
                    rrBuilder.append(stringArray[i + 1]);
                } else if (index >= 3 && index < 6) {
                    macBuilder.append(stringArray[i]);
                    macBuilder.append(stringArray[i + 1]);
                } else {
                    snrBuilder.append(stringArray[i]);
                    snrBuilder.append(stringArray[i + 1]);
                }

                i += 2;
            }
            meidBuilder.append(rrBuilder.toString());
            meidBuilder.append(macBuilder.toString());
            meidBuilder.append(snrBuilder.toString());
            return meidBuilder.toString();
        }
        return null;
    }

    private String parseWifiMac(byte[] data){
        byte[] realData = getSubData(data, 3);
        if(realData != null && realData.length > 11){
            StringBuilder wifiMacBuilder = new StringBuilder();
            String tmp;
            for (int i = 0; i < 6; i++)
            {
                if (i != 0)
                {
                    wifiMacBuilder.append(":");
                }
                tmp = Integer.toHexString(0xFF & realData[i]);
                if (tmp.length() == 1)// 每个字节8为，转为16进制标志，2个16进制位
                {
                    wifiMacBuilder.append("0");
                }
                wifiMacBuilder.append(tmp);
            }
            return wifiMacBuilder.toString();
        }
        return null;
    }
}
