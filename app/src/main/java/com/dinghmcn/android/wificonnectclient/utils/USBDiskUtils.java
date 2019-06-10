package com.dinghmcn.android.wificonnectclient.utils;

import android.content.Context;
import android.os.StatFs;

import com.dinghmcn.android.wificonnectclient.CITTestHelper;
import com.dinghmcn.android.wificonnectclient.DiskManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class USBDiskUtils {
    private static final String MOUNTS_FILE = "/proc/mounts";

    private static String path = "/mnt/usbhost1";
    public static USBDiskUtils instance;
    private Context mContext;
    public USBDiskUtils(Context mContext) {
        this.mContext = mContext;
        isMounted();
    }
    public static USBDiskUtils getInstance(Context mContext){
        if (instance==null)
            instance=new USBDiskUtils(mContext);
        return instance;
    }
    public  boolean isMounted() {

        boolean blnRet = false;
        String strLine = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));

            while ((strLine = reader.readLine()) != null) {
                if (strLine.contains(path)) {
                    blnRet = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }

        return blnRet;
    }

    public long getSDFreeSize(){
        if (!isMounted())
            return 0;
        path= DiskManager.getUsbStoragePath(CITTestHelper.getContext_x());
        StatFs sf = new StatFs(path);
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }
    public long getSDAllSize() {
        if (!isMounted())
            return 0;
        //取得SD卡文件路径
        path= DiskManager.getUsbStoragePath(CITTestHelper.getContext_x());
        StatFs sf = new StatFs(path);
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        //return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        return (allBlocks * blockSize) / 1024 / 1024; //单位MB

    }
}